/*
 * SAXXMLParser.java
 * Last modified on 04-08-2014 10:51-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.utilities;

import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;


public class SAXXMLParser {
    public static List<String> parse(InputStream is) {
        List<String> parsingStru = null;
        try {
            // create a XMLReader from SAXParser
            XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser()
                    .getXMLReader();
            // create a SAXXMLHandler
            SAXXMLHandler saxHandler = new SAXXMLHandler();
            // store handler in XMLReader
            xmlReader.setContentHandler(saxHandler);
            // the process starts
            xmlReader.parse(new InputSource(is));
            // get the `get list`
            parsingStru = new ArrayList<String>();
        } catch (Exception ex) {
            Log.d("aaa", "an exception here");
        }
        return parsingStru;
    }
}
