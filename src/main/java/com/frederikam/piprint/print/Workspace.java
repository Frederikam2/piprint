package com.frederikam.piprint.print;

import com.frederikam.piprint.svg.Svg;
import com.frederikam.piprint.svg.geom.CubicBezierCurve;
import com.frederikam.piprint.svg.geom.Line;
import com.frederikam.piprint.svg.geom.Point;
import com.frederikam.piprint.svg.geom.StraightLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Workspace {

    private static final Logger log = LoggerFactory.getLogger(Workspace.class);

    private final StepperMotor stepperX;
    private final StepperMotor stepperY;
    private final Servo servoMotor;
    private final int minStepInterval; // Where half speed is 2x this interval
    private final LinkedList<LinkedList<WorkPoint>> paths = new LinkedList<>();
    private final ExecutorService stepperExecutor = Executors.newFixedThreadPool(2);

    private Director director;

    /* Used to determine position */
    private Point nextPosition = new Point(0, 0);
    private Point lastPosition = new Point(0, 0);
    private long lastTime = System.currentTimeMillis();

    public Workspace(StepperMotor stepperX, StepperMotor stepperY, Servo servoMotor, Svg svg, int minStepInterval) {
        this.stepperX = stepperX;
        this.stepperY = stepperY;
        this.servoMotor = servoMotor;
        this.minStepInterval = minStepInterval;

        /* Compute points */
        svg.getPaths().forEach(path -> {
            paths.add(generatePath(path.getLines()));
        });

        try {
            log.info("Lifting servo");
            servoMotor.reset();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private LinkedList<WorkPoint> generatePath(List<Line> lines) {
        LinkedList<WorkPoint> points = new LinkedList<>();

        // Add start point
        points.add(new WorkPoint(lines.get(0).getStart(), false));

        for (Line line : lines) {
            if (line instanceof StraightLine) {
                boolean lifted = points.getLast().minus(line.getStart()).magnitude() > 1;

                points.add(new WorkPoint(
                        ((StraightLine) line).getEnd(),
                        !lifted));
            } else if (line instanceof CubicBezierCurve) {
                int desiredPoints = (int) (((CubicBezierCurve) line).estimateLength() / 4);
                desiredPoints = Math.max(1, desiredPoints);

                // Add just enough points to represent a curve, while minimizing rounding errors
                for (double i = 0; i < desiredPoints; i++) {
                    boolean lifted = i == 0 && points.getLast().minus(line.getStart()).magnitude() > 1;

                    points.add(new WorkPoint(
                            line.tween((i + 1) / desiredPoints),
                            !lifted));
                }
            }
        }

        // Scale
        points.replaceAll((point) -> new WorkPoint(point.multiply(4), point.lowered));

        return points;
    }

    public boolean isPaused() {
        return director == null || director.getState() != Thread.State.RUNNABLE;
    }

    public void setPause(boolean bool) {
        if (bool) {
            director.interrupt();
        } else {
            if (director != null && director.getState() != Thread.State.TERMINATED) return;

            // Begin the director
            director = new Director();
            director.start();
        }
    }

    /**
     * Tween between the last and target point, using the difference between the current time and the ETA.
     *
     * @return the estimated position
     */
    private WorkPoint getCurrentPosition() {
        Point posDiff = lastPosition.minus(nextPosition);
        double timeDiff = System.currentTimeMillis() - lastTime;
        double timeBetweenPoints = (posDiff.magnitude() * minStepInterval);

        return new WorkPoint(lastPosition.plus(posDiff.multiply(timeDiff / timeBetweenPoints)));
    }

    // Move synchronously with both steppers
    private void moveSync(int stepsX, int stepsY, int time) throws InterruptedException {
        log.info("Moving x: {}, y: {}, time: {}ms", stepsX, stepsY, time);
        Future futureX = stepperExecutor.submit(() -> runStepperX(stepsX, time));
        Future futureY = stepperExecutor.submit(() -> runStepperY(stepsY, time));

        try {
            futureX.get();
            futureY.get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void runStepperX(int steps, int time) {
        if (time == 0 || steps == 0) return;
        // Ranging from 0 to 1
        double speed = (minStepInterval * ((double) Math.abs(steps))) / ((double) time);
        double interval = minStepInterval / speed;
        // Stepper X supports variable speed, unlike stepper Y which behaves weird
        stepperX.step(steps, (int) interval);
    }

    private void runStepperY(int steps, int time) {
        if (time == 0 || steps == 0) return;

        // Stepper Y steps at a constant interval of 4ms.
        double interval = 4;
        double targetCycleSteps = 25; // 1/16 revolution
        double cycles = steps / targetCycleSteps;

        // Adjust the number of cycles to a non-zero integer
        cycles = Math.max(1, Math.round(cycles));
        int stepsPerCycle = (int) (steps / cycles);
        //noinspection CodeBlock2Expr
        try {
            new NanosecondExecutor(() -> {
                stepperY.step(stepsPerCycle, (long) interval);
            }, (int) cycles, (int) ((time / cycles) * 1000000))
                    .run();
        } catch (InterruptedException e) {
            log.error("Interrupted while running stepper", e);
        }
    }

    /**
     * The Thread responsible for making sure we are going in the right direction
     */
    private class Director extends Thread {

        Director() {
            setName("Director");
            setDaemon(false);
            setPriority(Thread.MAX_PRIORITY);
        }

        @Override
        public void run() {
            int i = 0;
            int size = paths.size();
            try {
                while (!paths.isEmpty()) {
                    i++;
                    log.info("Began drawing path {} of {}", i, size);
                    drawPath(paths.removeFirst());
                    servoMotor.setLowered(false);
                }
                // Move back to 0,0
                goToPoint(new Point(0, 0));
                System.exit(0);
            } catch (InterruptedException e) {
                if (servoMotor.isLowered()) {
                    // We are currently drawing! Make sure we return to our current position if we continue drawing
                    paths.get(0).remove(0);
                    paths.get(0).add(0, getCurrentPosition());
                }
            }
            log.info("Finished drawing {} paths", size);
            try {
                servoMotor.setLowered(false);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        private void drawPath(LinkedList<WorkPoint> path) throws InterruptedException {
            while (!path.isEmpty()) {
                WorkPoint pos = path.removeFirst();
                servoMotor.setLowered(pos.lowered);
                nextPosition = pos;
                goToPoint(pos);
                lastPosition = pos;
                lastTime = System.currentTimeMillis();
            }
            servoMotor.setLowered(false);
        }

        private void goToPoint(Point point) throws InterruptedException {
            Point diff = point.minus(lastPosition);
            int time = (int) Math.max(
                    Math.abs(diff.getX() * minStepInterval),
                    Math.abs(diff.getY() * 4));

            moveSync((int) diff.getX(),
                    (int) diff.getY(),
                    time);
        }
    }

    private class WorkPoint extends Point {

        boolean lowered = true;

        WorkPoint(Point p) {
            super(p.getX(), p.getY());
        }

        WorkPoint(Point p, boolean lowered) {
            super(p.getX(), p.getY());
            this.lowered = lowered;
        }

        @Override
        public String toString() {
            return "WorkPoint{" +
                    "x=" + this.getX() +
                    " y=" + this.getY() +
                    " lowered=" + lowered +
                    '}';
        }
    }
}
