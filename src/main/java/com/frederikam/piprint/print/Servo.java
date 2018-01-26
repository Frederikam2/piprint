package com.frederikam.piprint.print;

import com.pi4j.io.gpio.GpioPinDigitalOutput;

public class Servo {

    private static final double UP = 0;
    private static final double DOWN = 2;
    private static final long moveTime = 500;

    private boolean lowered = false;
    private volatile double pulseWidth = 0; // As % of cycle
    private volatile long stopTime = 0;

    public Servo(GpioPinDigitalOutput pin, double cycleFrequency) {
        Thread servoThread = new Thread(() -> {
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    if (stopTime < System.currentTimeMillis() && pulseWidth == UP) {
                        pin.low();
                    } else {
                        pin.high();
                    }
                    double lowTime = 1000 / cycleFrequency - pulseWidth;
                    long upMs = (long) pulseWidth; // Up time miliseconds
                    int upNanos = ((int) pulseWidth * 1000000 % 1000000); // Up time nanoseconds
                    Thread.sleep(upMs, upNanos);
                    pin.low();
                    long lowMs = (long) lowTime;
                    int lowNanos = (int) (lowTime * 1000000 % 1000000);

                    java.lang.Thread.sleep(lowMs, lowNanos);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        servoThread.start();
    }

    public void setLowered(boolean b) throws InterruptedException {
        if (b == lowered) return;

        lowered = b;
        pulseWidth = b ? DOWN : UP;
        Thread.sleep(moveTime);

        stopTime = System.currentTimeMillis() + 300;
    }

    public boolean isLowered() {
        return lowered;
    }

    public void reset() throws InterruptedException {
        lowered = false;
        pulseWidth = UP;
        stopTime = System.currentTimeMillis() + 300;
        Thread.sleep(moveTime);
    }
}
