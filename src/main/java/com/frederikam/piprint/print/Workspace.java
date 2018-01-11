package com.frederikam.piprint.print;

import com.frederikam.piprint.svg.Svg;
import com.frederikam.piprint.svg.geom.Point;

import java.util.LinkedList;

public class Workspace {

    private final Svg svg;
    private final StepperMotor stepperX;
    private final StepperMotor stepperY;
    private final Servo servoMotor;
    private final int minStepInterval; // Where half speed is 2x this interval
    private final LinkedList<LinkedList<Point>> paths = new LinkedList<>();

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
                for (double i = 0; i < 201; i++) {
                    newPath.add(line.tween(i/200d));
                }
            });

            paths.add(newPath);
        });

        // Make sure we are at 0
        stepperX.step(-1000, minStepInterval);
        stepperY.step(-1000, minStepInterval);

        try {
            servoMotor.reset();
            Thread.sleep(Math.max(0, 1000 * minStepInterval - Servo.DELAY));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPause(boolean bool) {
        if (bool) {
            if (director != null && director.getState() != Thread.State.TERMINATED) return;

            // Begin the director
            director = new Director();
            director.start();
        } else {
            director.interrupt();
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

    /**
     * The Thread responsible for making sure we are going in the right direction
     */
    private class Director extends Thread {

        Director() {
            setName("Director");
            setPriority(Thread.MAX_PRIORITY);
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < paths.size(); i++) {
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

            // Must not multiply by zero
            Point diff = point.minus(lastPosition);
            long time;
            if (intervalX != 0) {
                time = (long) (intervalX * diff.getX());
            } else if (intervalY != 0) {
                time = (long) (intervalY * diff.getY());
            } else {
                // Distance is zero
                return;
            }

            stepperX.step((long) diff.getX(), intervalX);
            stepperY.step((long) diff.getY(), intervalY);

            Thread.sleep(time);
        }
    }
}
