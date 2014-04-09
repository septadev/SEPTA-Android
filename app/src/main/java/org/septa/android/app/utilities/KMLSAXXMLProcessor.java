/*
 * SAXXMLHandler.java
 * Last modified on 04-06-2014 12:19-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.utilities;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Stack;

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

    private String contentsOfKMLFile;

    public KMLSAXXMLProcessor(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public void readKMLFile(String kmlFileName) {
        Document xmlDocument = null;
        KMLModel kmlModel = new KMLModel();
        kmlModel.createDocument();

        try {
            // using the asset manager, get the input stream to the KML file.
            InputStream input = assetManager.open(kmlFileName);

            // create a document builder to create the XML document object.
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            xmlDocument = builder.parse(new InputSource(input));

            // create the xPath object.
            XPath xPath=XPathFactory.newInstance().newXPath();


            // get and set the style color on the line style from the Document
            KMLModel.Document document = kmlModel.getDocument();
            document.createStyle();
            document.getStyle().createLineStyle();
            XPathExpression tag_id = xPath.compile("/kml/Document/Style/LineStyle/color");
            document.getStyle().getLineStyle().setColor(tag_id.evaluate(xmlDocument));

            tag_id = xPath.compile("//Placemark");
            NodeList placemarkNodeList = (NodeList) tag_id.evaluate(xmlDocument, XPathConstants.NODESET);

            Log.d(TAG, "the size of the placemarkNodeList is "+placemarkNodeList.getLength());
            document.createPlacemarks();
            for (int i = 0; i < placemarkNodeList.getLength(); i++) {
                Log.d(TAG, "in placemark node list loop");
                KMLModel.Document.Placemark placemark = new KMLModel.Document.Placemark();

                Node n = placemarkNodeList.item(i);

                tag_id = xPath.compile("name");
                String name = tag_id.evaluate(n);
                placemark.setName(name);

                tag_id = xPath.compile("MultiGeometry/LineString/coordinates");
                NodeList coordinatesNodeList = (NodeList) tag_id.evaluate(n, XPathConstants.NODESET);

                placemark.createMultiGeometry();

                KMLModel.Document.MultiGeometry.LineString lineString = placemark.getMultiGeometry().createLineString();
                for (int j = 0; j < coordinatesNodeList.getLength(); i++) {
                    Log.d(TAG, "in coordinates node list loop");
                    String coordinatesString = coordinatesNodeList.item(j).getFirstChild().getNodeValue();
                    Log.d(TAG, "coordinates string value is "+coordinatesString);
                    lineString.setRawCoordinateString(coordinatesString.trim());

                    lineString.processRawCoordinatesString();
                }

                document.getPlacemarks().add(placemark);
            }
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();;
        }
    }
}