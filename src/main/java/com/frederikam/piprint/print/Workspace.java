package com.frederikam.piprint.print;

import com.frederikam.piprint.svg.Svg;
import com.frederikam.piprint.svg.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Workspace {

    private static final Logger log = LoggerFactory.getLogger(Workspace.class);

    private final Svg svg;
    private final StepperMotor stepperX;
    private final StepperMotor stepperY;
    private final Servo servoMotor;
    private final int minStepInterval; // Where half speed is 2x this interval
    private final LinkedList<LinkedList<Point>> paths = new LinkedList<>();
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
        this.svg = svg;
        this.minStepInterval = minStepInterval;

        /* Compute points */
        svg.getPaths().forEach(path -> {
            LinkedList<Point> newPath = new LinkedList<>();

            path.getLines().forEach(line -> {
                for (double i = 0; i < 2; i++) {
                    newPath.add(line.tween(i/200d));
                }
            });

            paths.add(newPath);
        });

        try {
            log.info("Lifting servo");
            servoMotor.reset();

            log.info("Moving to 0,0");
            moveSync(-1600, minStepInterval,
                    -1600, minStepInterval);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
    private Point getCurrentPosition() {
        Point posDiff = lastPosition.minus(nextPosition);
        double timeDiff = System.currentTimeMillis() - lastTime;
        double timeBetweenPoints = (posDiff.magnitude() * minStepInterval);

        return lastPosition.plus(posDiff.multiply(timeDiff / timeBetweenPoints));
    }

    // Move synchronously with both steppers
    private void moveSync(int stepsX, int intervalX, int stepsY, int intervalY) throws InterruptedException {
        Future futureX = stepperExecutor.submit(() -> stepperX.step(stepsX, intervalX));
        Future futureY = stepperExecutor.submit(() -> stepperY.step(stepsY, intervalY));

        try {
            futureX.get();
            futureY.get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
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
                while(!paths.isEmpty()) {
                    i++;
                    log.info("Began drawing path {} of {}", i, size);
                    drawPath(paths.removeFirst());
                    servoMotor.setLowered(false);
                }
            } catch (InterruptedException e) {
                if (servoMotor.isLowered()) {
                    // We are currently drawing! Make sure we return to our current position if we continue drawing
                    paths.get(0).remove(0);
                    paths.get(0).add(0, getCurrentPosition());
                }
            }
            log.info("Finished drawing {} paths", size);
        }

        private void drawPath(LinkedList<Point> path) throws InterruptedException {
            for (int i = 0; i < path.size(); i++) {
                Point pos = path.removeFirst();
                nextPosition = pos;
                goToPoint(pos);
                lastPosition = pos;
                lastTime = System.currentTimeMillis();
            }
        }

        private void goToPoint(Point point) throws InterruptedException {
            Point unit = point.unit();

            // Must not divide by zero
            int intervalX = 0;
            int intervalY = 0;
            if (intervalX != unit.getX()) intervalX = (int) (minStepInterval / unit.getX());
            if (intervalY != unit.getY()) intervalY = (int) (minStepInterval / unit.getY());

            Point diff = point.minus(lastPosition);

            moveSync((int) diff.getX(), intervalX,
                    (int) diff.getY(), intervalY);
        }
    }
}
