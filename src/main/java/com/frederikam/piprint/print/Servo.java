package com.frederikam.piprint.print;

import com.pi4j.io.gpio.GpioPinDigitalOutput;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Servo {

    private static final double UP = 0;
    private static final double DOWN = 1;
    private static final long moveTime = 1000;

    private boolean lowered = false;
    private final double cycleLengthMs;
    private volatile double pulseWidth = 0; // As % of cycle

    public Servo(GpioPinDigitalOutput pin, double cycleFrequency) {
        cycleLengthMs = 1000d / cycleFrequency;
        Runnable task = () -> pin.pulse((long) (cycleLengthMs * pulseWidth), false);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor((r) -> {
            Thread t = new Thread();
            t.setDaemon(true);
            t.setName("Servo");
            return t;
        });
        executor.scheduleAtFixedRate(task, 0, (long) cycleLengthMs, TimeUnit.MILLISECONDS);
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
