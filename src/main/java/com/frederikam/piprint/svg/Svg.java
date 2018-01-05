package com.frederikam.piprint.svg;

import com.frederikam.piprint.svg.geom.Dimension;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Represent an SVG 2.0 graphic
 */
public class Svg {

    private final ArrayList<Path> paths = new ArrayList<>();
    private final Dimension viewBox;

    public Svg(String xml) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory dbf =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(xml));

        Document doc = db.parse(is);
        NodeList nodes = doc.getElementsByTagName("path");

        String vb = doc.getDocumentElement().getAttribute("viewBox");
        List<Double> vbArgs = SvgUtil.parsePathCommandArgs(vb);
        viewBox = new Dimension(vbArgs.get(2), vbArgs.get(3));

        for (int i = 0; i < nodes.getLength(); i++) {
            paths.add(new Path(nodes.item(i)));
        }
    }

    public ArrayList<Path> getPaths() {
        return paths;
    }

    public Dimension getViewBox() {
        return viewBox;
    }
}
