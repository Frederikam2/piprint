package com.frederikam.piprint.svg;

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

/**
 * Represent an SVG 1.2 graphic
 */
public class Svg {

    private final ArrayList<Path> paths = new ArrayList<>();

    public Svg(String xml) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory dbf =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(xml));

        Document doc = db.parse(is);
        NodeList nodes = doc.getElementsByTagName("path");
        for (int i = 0; i < nodes.getLength(); i++) {
            paths.add(new Path(nodes.item(i)));
        }
    }

    public ArrayList<Path> getPaths() {
        return paths;
    }
}
