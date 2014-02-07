/*
 * TextImageModel.java
 * Last modified on 02-07-2014 16:00-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models.adapterhelpers;

public class TextImageModel {
    private String mainText;
    private String imageNameBase;
    private String imageNameSuffix;

    public TextImageModel(String mainText, String imageNameBase, String imageNameSuffix) {
        this.mainText = mainText;
        this.imageNameBase = imageNameBase;
        this.imageNameSuffix = imageNameSuffix;
    }

    public String getMainText() {

        return this.mainText;
    }

    public String getImageNameBase() {

        return this.imageNameBase;
    }

    public String getImageNameSuffix() {

        return this.imageNameSuffix;
    }
}
