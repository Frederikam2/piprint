package com.frederikam.piprint;

import com.frederikam.piprint.print.Servo;
import com.frederikam.piprint.print.StepperMotor;
import com.frederikam.piprint.print.Workspace;
import com.frederikam.piprint.svg.Svg;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static final GpioController gpio = GpioFactory.getInstance();
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        String arg1 = "";
        if (args.length > 0)
            arg1 = args[0];

        switch (arg1) {
            case "print":
                if (args.length < 2) {
                    log.error("Correct usage: java -jar piprint.jar print <path>");
                }

                File file = new File(args[1]);

                if (!file.exists()) {
                    log.error("File does not exist: " + args[1]);
                }

                StepperMotor s1 = new StepperMotor(RaspiPin.GPIO_00, RaspiPin.GPIO_01, RaspiPin.GPIO_02, RaspiPin.GPIO_03);
                StepperMotor s2 = new StepperMotor(RaspiPin.GPIO_04, RaspiPin.GPIO_05, RaspiPin.GPIO_06, RaspiPin.GPIO_07);
                Servo servo = new Servo(RaspiPin.GPIO_08);
                String xml = new String(Files.readAllBytes(Paths.get(args[1])), StandardCharsets.UTF_8);
                Svg svg = new Svg(xml);
                Workspace workspace = new Workspace(s1, s2, servo, svg, 2);
                workspace.setPause(false);
                break;
            case "steptest":
                new StepTest().run();
                break;
            case "svgtest":
                new SvgTest().run(args);
                break;
        }
    }

}
