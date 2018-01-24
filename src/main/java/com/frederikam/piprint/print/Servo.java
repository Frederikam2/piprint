package com.frederikam.piprint.print;

import com.frederikam.piprint.Main;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Servo {

    private static final Logger log = LoggerFactory.getLogger(Servo.class);
    private static final double UP = 1.9;
    private static final double DOWN = 2;
    private static final long moveTime = 2000;

    private boolean lowered = false;
    private volatile double pulseWidth = 0; // As % of cycle

    public Servo(GpioPinDigitalOutput pin, double cycleFrequency) {
        Main.gpio.
        Thread servoThread = new Thread(() -> {
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    pin.high();
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
    }

    public boolean isLowered() {
        return lowered;
    }

    public void reset() throws InterruptedException {
        lowered = false;
        pulseWidth = DOWN;
        Thread.sleep(moveTime);
    }
}
