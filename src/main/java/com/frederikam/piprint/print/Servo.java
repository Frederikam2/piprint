package com.frederikam.piprint.print;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Servo {

    private static final Logger log = LoggerFactory.getLogger(Servo.class);
    private static final double UP = 0.2;
    private static final double DOWN = 1;
    private static final long moveTime = 2000;

    private boolean lowered = false;
    private final long cycleLengthMs;
    private volatile double pulseWidth = 0; // As % of cycle

    public Servo(GpioPinDigitalOutput pin, double cycleFrequency) {
        cycleLengthMs = (long) (1000d / cycleFrequency);

        Runnable task = () -> {
            try {
                log.info("pulse {}", pulseWidth);
                if(pulseWidth > 0)
                    pin.pulse((long) (cycleLengthMs * pulseWidth), false);
            } catch (RuntimeException e) {
                log.info("Exception while running servo", e);
            }
        };
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor((r) -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("Servo");
            return t;
        });
        executor.scheduleAtFixedRate(task, 0, cycleLengthMs, TimeUnit.MILLISECONDS);
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
