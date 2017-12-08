package com.frederikam.piprint;

import com.frederikam.piprint.print.StepperMotor;
import com.pi4j.io.gpio.RaspiPin;

public class StepTest implements Runnable {

    @Override
    public void run() {
        System.out.println("Running steptest");

        StepperMotor stepperMotor = new StepperMotor(RaspiPin.GPIO_00, RaspiPin.GPIO_01, RaspiPin.GPIO_02, RaspiPin.GPIO_03);
        stepperMotor.step(400, 2);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
