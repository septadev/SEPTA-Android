/*
 * SAXXMLHandler.java
 * Last modified on 04-08-2014 10:49-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.utilities;

import android.util.Log;

import org.septa.android.app.models.KMLModel;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class SAXXMLHandler extends DefaultHandler {
    private static final String TAG = SAXXMLHandler.class.getName();

    private KMLModel kmlModel = new KMLModel();

    public SAXXMLHandler() { }

    public KMLModel getKMLModel() {
        return kmlModel;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        kmlModel.createDocument();

        Log.d(TAG, "start the document");
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {

        Log.d(TAG, "start qName and localName is " + qName + " " + localName);

        if (qName.equalsIgnoreCase("Placemark")) {
//            tempStr = new ParsingStructure();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        Log.d(TAG, "processing the characters");
//        if (tempVal != null) {
//            for (int i=start; i<start+length; i++) {
//                tempVal.append(ch[i]);
//            }
//        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        Log.d(TAG, "end qName and localName is " + qName + " " + localName);
//        if (qName.equalsIgnoreCase("Placemark")) {
//            // add it to the list
//            parsingStructure.add(tempStr);
//        } else if (qName.equalsIgnoreCase("name")) {
//            if(tempStr != null)
//                tempStr.setName(tempVal.toString());
//        } else if (qName.equalsIgnoreCase("description")) {
//            if(tempStr != null)
//                tempStr.setDescription(tempVal.toString());
//        } else if (qName.equalsIgnoreCase("coordinates")) {
//            if(tempStr != null)
//                tempStr.setCoordinates(tempVal.toString());
//        } else if(qName.equalsIgnoreCase("kml")) {
//
//        }
    }

    public void warning(SAXParseException e) throws SAXException {
        Log.d(TAG, "warning");
    }

    public void error(SAXParseException e) throws SAXException {
        Log.d(TAG, "error");
    }

    public void fatalError(SAXParseException e) throws SAXException {
        Log.d(TAG, "fatalerror");
    }
}
