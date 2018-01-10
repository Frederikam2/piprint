package com.frederikam.piprint.print;

import com.pi4j.component.motor.impl.GpioStepperMotorComponent;
import com.pi4j.io.gpio.*;

public class StepperMotor {

    private final GpioController gpio = GpioFactory.getInstance();
    private final GpioPinDigitalOutput pin1;
    private final GpioPinDigitalOutput pin2;
    private final GpioPinDigitalOutput pin3;
    private final GpioPinDigitalOutput pin4;
    private final GpioStepperMotorComponent motor;

    private final byte[] singleStepForwardSeq;
    private final byte[] singleStepBackwardSeq;

    public StepperMotor(Pin pin1, Pin pin2,
                        Pin pin3, Pin pin4) {
        this.pin1 = gpio.provisionDigitalOutputPin(pin1, PinState.LOW);
        this.pin2 = gpio.provisionDigitalOutputPin(pin3, PinState.LOW);
        this.pin3 = gpio.provisionDigitalOutputPin(pin2, PinState.LOW);
        this.pin4 = gpio.provisionDigitalOutputPin(pin4, PinState.LOW);

        final GpioPinDigitalOutput[] pins = {
                this.pin1,
                this.pin2,
                this.pin3,
                this.pin4};

        // this will ensure that the motor is stopped when the program terminates
        gpio.setShutdownOptions(true, PinState.LOW, pins);

        // create motor component
        motor = new GpioStepperMotorComponent(pins);

        // create byte array to demonstrate a single-step sequencing
        // (This is the most basic method, turning on a single electromagnet every time.
        //  This sequence requires the least amount of energy and generates the smoothest movement.)
        singleStepForwardSeq = new byte[4];
        singleStepForwardSeq[0] = (byte) 0b0001;
        singleStepForwardSeq[1] = (byte) 0b0010;
        singleStepForwardSeq[2] = (byte) 0b0100;
        singleStepForwardSeq[3] = (byte) 0b1000;

        singleStepBackwardSeq = new byte[4];
        singleStepBackwardSeq[0] = singleStepForwardSeq[3];
        singleStepBackwardSeq[1] = singleStepForwardSeq[2];
        singleStepBackwardSeq[2] = singleStepForwardSeq[1];
        singleStepBackwardSeq[3] = singleStepForwardSeq[0];

        motor.setStepsPerRevolution(200);
    }

    public void step(long steps, long stepInterval) {
        motor.setStepSequence(singleStepForwardSeq);
        //motor.setStepSequence(steps > 0 ? singleStepForwardSeq : singleStepBackwardSeq);

        // define stepper parameters before attempting to control motor
        // anything lower than 2 ms does not work for my sample motor using single step sequence
        motor.setStepInterval(stepInterval);
        motor.step(steps);
    }

    public void stop() {
        motor.stop();
    }
}
