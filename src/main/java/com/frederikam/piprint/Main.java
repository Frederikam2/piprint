package com.frederikam.piprint;

import com.frederikam.piprint.svg.Path;
import com.frederikam.piprint.svg.Svg;
import com.frederikam.piprint.svg.geom.Line;
import com.frederikam.piprint.svg.geom.Point;
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

                    BufferedImage image = new BufferedImage(
                            (int) svg.getViewBox().getWidth(),
                            (int) svg.getViewBox().getHeight(),
                            BufferedImage.TYPE_INT_ARGB);
                    Graphics graphics = image.getGraphics();

                    for (Path path : svg.getPaths()) {
                        for (Line line : path.getLines()) {
                            for (int i = 0; i < 201; i++) {
                                double t = ((double) i) / 200;
                                Point point = line.tween(t);
                                graphics.setColor(line.getColor());
                                graphics.fillOval(
                                        (int) point.getX(),
                                        (int) point.getY(),
                                        2,
                                        2);
                            }

                            /*if (line instanceof CubicBezierCurve) {
                                CubicBezierCurve curve = (CubicBezierCurve) line;
                                graphics.fillOval((int) curve.p1.getX()*2, (int) curve.p1.getY()*2, 6,6);
                                graphics.fillOval((int) curve.p2.getX()*2, (int) curve.p2.getY()*2, 6,6);
                                graphics.fillOval((int) curve.p3.getX()*2, (int) curve.p3.getY()*2, 6,6);
                                graphics.fillOval((int) curve.p4.getX()*2, (int) curve.p4.getY()*2, 6,6);


                                graphics.drawLine(
                                        (int) curve.p1.getX()*2,
                                        (int) curve.p1.getY()*2,
                                        (int) curve.p2.getX()*2,
                                        (int) curve.p2.getY()*2);
                                graphics.setColor(Color.BLUE);
                                graphics.drawLine(
                                        (int) curve.p3.getX()*2,
                                        (int) curve.p3.getY()*2,
                                        (int) curve.p4.getX()*2,
                                        (int) curve.p4.getY()*2);

                            }*/
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
