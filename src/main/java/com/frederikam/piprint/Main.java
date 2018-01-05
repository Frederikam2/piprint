package com.frederikam.piprint;

import com.frederikam.piprint.svg.Path;
import com.frederikam.piprint.svg.Svg;
import com.frederikam.piprint.svg.geom.Line;
import com.frederikam.piprint.svg.geom.StraightLine;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
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

                    BufferedImage image = new BufferedImage(245, 250, BufferedImage.TYPE_INT_ARGB);
                    Graphics graphics = image.getGraphics();
                    graphics.setColor(Color.BLACK);

                    for (Path path : svg.getPaths()) {
                        for (Line line : path.getLines()) {
                            if (line instanceof StraightLine) {
                                StraightLine l = (StraightLine) line;
                                graphics.drawLine(
                                        (int) l.getStart().getX(),
                                        (int) l.getStart().getY(),
                                        (int) l.getEnd().getX(),
                                        (int) l.getEnd().getY());
                            }
                        }
                    }

                    ImageIO.write(image, "png", new File("test.png"));

                } catch (IOException | SAXException | ParserConfigurationException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

}
