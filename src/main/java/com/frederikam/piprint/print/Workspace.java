package com.frederikam.piprint.print;

import com.frederikam.piprint.svg.Svg;
import com.frederikam.piprint.svg.geom.Point;

import java.util.LinkedList;

public class Workspace {

    private final Svg svg;
    private final StepperMotor stepperX;
    private final StepperMotor stepperY;
    private final ServoMotor servoMotor;
    private final int minStepInterval; // Where half speed is 2x this interval
    private final LinkedList<LinkedList<Point>> paths = new LinkedList<>();

    private Director director;

    /* Used to determine position */
    private double nextPosition = 0; // Radians
    private Point lastPosition = new Point(0, 0);
    private long lastTime = System.nanoTime();

    public Workspace(StepperMotor stepperX, StepperMotor stepperY, ServoMotor servoMotor, Svg svg, int minStepInterval) {
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
            Thread.sleep(Math.max(0, 1000 * minStepInterval - ServoMotor.DELAY));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Begin the director
        director = new Director();
        director.start();
    }

    public Point getCurrentPosition() {

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

        private void drawPath(LinkedList<Point> path) {
            for (int i = 0; i < path.size(); i++) {
                Point pos = path.removeFirst();
                goToPoint(pos);
                lastPosition = pos;
                lastTime = System.nanoTime();
            }
        }

        private void goToPoint(Point point) {

        }
    }
}
