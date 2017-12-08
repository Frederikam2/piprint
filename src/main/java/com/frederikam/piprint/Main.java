package com.frederikam.piprint;

import com.frederikam.piprint.svg.Path;
import com.frederikam.piprint.svg.Svg;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        String arg1 = "gui";
        if (args.length > 0)
            arg1 = args[0];

        switch (arg1) {
            case "gui":
                //TODO
                break;
            case "steptest":
                new StepTest().run();
                break;
            case "svgtest":
                try {
                    String xml = new String(Files.readAllBytes(Paths.get(args[1])), StandardCharsets.UTF_8);
                    Svg svg = new Svg(xml);

                    System.out.println("Found " + svg.getPaths().size() + " paths");
                    for (Path path : svg.getPaths()) {
                        System.out.println(path.getLines());
                    }
                } catch (IOException | SAXException | ParserConfigurationException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

}
