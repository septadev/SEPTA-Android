/*
 * SAXXMLHandler.java
 * Last modified on 04-06-2014 12:19-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.utilities;

import android.content.res.AssetManager;
import android.nfc.Tag;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.septa.android.app.models.KMLModel;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

public class KMLSAXXMLProcessor {
    public static final String TAG = KMLSAXXMLProcessor.class.getName();

    private KMLModel kmlModel;
    private AssetManager assetManager;

    public KMLSAXXMLProcessor(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public KMLModel getKMLModel() {

        return kmlModel;
    }

    public void readKMLFile(String kmlFileName) {
        Document xmlDocument = null;

        kmlModel = new KMLModel();
        KMLModel.Document document = kmlModel.createDocument();
        try {
            // using the asset manager, get the input stream to the KML file.
            InputStream input = assetManager.open(kmlFileName);

            // create a document builder to create the XML document object.
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            xmlDocument = builder.parse(new InputSource(input));

            // create the xPath object.
            XPath xPath=XPathFactory.newInstance().newXPath();

            document.createStyleList();

            //document/style
            XPathExpression tag_id = xPath.compile("/kml/Document/Style");
            NodeList styleNodeList = (NodeList) tag_id.evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < styleNodeList.getLength(); i++) {
                KMLModel.Document.Style style = new KMLModel.Document.Style();
                Node n = styleNodeList.item(i);

                String id = n.getAttributes().getNamedItem("id").getNodeValue();
                style.setId(id);

                tag_id = xPath.compile("LineStyle/color");
                String lineStyleColor = tag_id.evaluate(n);

                String repositionedColorString = lineStyleColor.substring(0,2) +
                                                 lineStyleColor.substring(6,8) +
                                                 lineStyleColor.substring(4,6) +
                                                 lineStyleColor.substring(2,4);

                style.createLineStyle().setColor(repositionedColorString);

                document.getStyleList().add(style);
            }

            tag_id = xPath.compile("//Placemark");
            NodeList placemarkNodeList = (NodeList) tag_id.evaluate(xmlDocument, XPathConstants.NODESET);

            ArrayList<KMLModel.Document.Placemark> placemarks = (ArrayList<KMLModel.Document.Placemark>) document.createPlacemarkList();
            for (int i = 0; i < placemarkNodeList.getLength(); i++) {
                KMLModel.Document.Placemark placemark = new KMLModel.Document.Placemark();

                Node n = placemarkNodeList.item(i);

                tag_id = xPath.compile("name");
                String name = tag_id.evaluate(n);
                placemark.setName(name);

                tag_id = xPath.compile("styleUrl");
                String styleUrl = tag_id.evaluate(n).substring(1);
                placemark.setStyleUrl(styleUrl);

                tag_id = xPath.compile("MultiGeometry/LineString/coordinates");
                NodeList coordinatesNodeList = (NodeList) tag_id.evaluate(n, XPathConstants.NODESET);

                ArrayList<KMLModel.Document.MultiGeometry.LineString> lineStrings = (ArrayList<KMLModel.Document.MultiGeometry.LineString>) placemark.createMultiGeometry().createLineStringList();
                for (int j = 0; j < coordinatesNodeList.getLength(); j++) {
                    KMLModel.Document.MultiGeometry.LineString lineString = new KMLModel.Document.MultiGeometry.LineString();
                    String coordinatesString = coordinatesNodeList.item(j).getFirstChild().getNodeValue();
                    lineString.setRawCoordinateString(coordinatesString.trim());

                    lineString.processRawCoordinatesString();

                    lineStrings.add(lineString);
                }

                placemarks.add(placemark);
            }
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();;
        }
    }
}