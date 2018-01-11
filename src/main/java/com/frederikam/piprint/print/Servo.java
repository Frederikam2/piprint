package com.frederikam.piprint.print;

import com.pi4j.component.servo.ServoDriver;
import com.pi4j.component.servo.impl.GenericServo;
import com.pi4j.io.gpio.Pin;

public class Servo {

    /**
     * Time it takes to move into position
     */
    static final int DELAY = 1000;

    private boolean lowered = false;
    private Pin pin;
    private final GenericServo servo;

    public Servo(Pin servoPin) {
        servo = new GenericServo(new Driver(), "servo");
        this.pin = servoPin;
    }

    public boolean isLowered() {
        return lowered;
    }

    public void setLowered(boolean bool) throws InterruptedException {
        if (bool != lowered) {
            lowered = bool;
            servo.setPosition(bool ? -100 : 100);
            Thread.sleep(DELAY);
        }
    }

    public void reset() throws InterruptedException {
        lowered = false;
        Thread.sleep(DELAY);
    }

    public class Driver implements ServoDriver {

        @Override
        public int getServoPulseWidth() {
            return lowered ? 100 : 200;
        }

        @Override
        public void setServoPulseWidth(int width) { /* Ignored */ }

        @Override
        public int getServoPulseResolution() {
            return 100;
        }

        @Override
        public Pin getPin() {
            return pin;
        }
    }

}
