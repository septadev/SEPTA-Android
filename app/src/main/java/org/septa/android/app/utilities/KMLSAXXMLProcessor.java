/*
 * SAXXMLHandler.java
 * Last modified on 04-06-2014 12:19-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.utilities;

import org.septa.android.app.models.KMLModel;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class KMLSAXXMLProcessor extends DefaultHandler {
    private KMLModel kmlModel;

    private String tempVal;

    private String previousElement;

    public KMLSAXXMLProcessor() {
        kmlModel = new KMLModel();
    }

    public KMLModel getKmlModel() {
        return kmlModel;
    }

    // Event Handlers
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        // reset
        tempVal = "";
        if (qName.equalsIgnoreCase("kml")) {
            // create a new instance of employee
            tempEmp = new Employee();
        }
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (qName.equalsIgnoreCase("employee")) {
            // add it to the list
            employees.add(tempEmp);
        } else if (qName.equalsIgnoreCase("id")) {
            tempEmp.setId(Integer.parseInt(tempVal));
        } else if (qName.equalsIgnoreCase("name")) {
            tempEmp.setName(tempVal);
        } else if (qName.equalsIgnoreCase("department")) {
            tempEmp.setDepartment(tempVal);
        } else if (qName.equalsIgnoreCase("type")) {
            tempEmp.setType(tempVal);
        } else if (qName.equalsIgnoreCase("email")) {
            tempEmp.setEmail(tempVal);
        }
    }
}