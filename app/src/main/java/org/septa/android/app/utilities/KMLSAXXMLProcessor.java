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
import java.io.Reader;
import java.io.StringReader;

import org.septa.android.app.models.KMLModel;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

public class KMLSAXXMLProcessor {
    public static final String TAG = KMLSAXXMLProcessor.class.getName();

    private KMLModel kmlModel;
    private AssetManager assetManager;

    private String contentsOfKMLFile;

    public KMLSAXXMLProcessor(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public void readKMLFile(String kmlFileName) {
        // To load text file
        InputStream input;
        try {
            input = assetManager.open(kmlFileName);

            int size = input.available();
            Log.d(TAG, "reporting the size as "+size);
            byte[] buffer = new byte[size];
            input.read(buffer);

            input.close();

            // byte buffer into a string
            contentsOfKMLFile = new String(buffer);

            Serializer serializer = new Persister();
            Reader reader = new StringReader(contentsOfKMLFile);

            KMLModel kmlModel = serializer.read(KMLModel.class, reader, false);
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();;
        }

        Log.d(TAG, "successfully read the file as:|"+contentsOfKMLFile+"|");
    }

}