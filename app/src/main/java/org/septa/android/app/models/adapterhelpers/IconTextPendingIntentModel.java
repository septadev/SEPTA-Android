/*
 * IconTextPendingIntentModel.java
 * Last modified on 02-09-2014 20:40-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models.adapterhelpers;

import android.app.PendingIntent;
import android.net.Uri;

public class IconTextPendingIntentModel {
    private String text;
    private String iconImageNameBase;
    private String iconImageNameSuffix;
    private boolean enabled;
    private String urlToLoad;

    public IconTextPendingIntentModel(String text, String iconImageNameBase, String iconImageNameSuffix, String urlToLoad, boolean enabled) {
        this.text = text;
        this.iconImageNameBase = iconImageNameBase;
        this.iconImageNameSuffix = iconImageNameSuffix;
        this.urlToLoad = urlToLoad;
        this.enabled = enabled;
    }

    public String getText() {

        return this.text;
    }

    public String getIconImageNameBase() { return this.iconImageNameBase; }

    public String getIconImageNameSuffix() {



        return this.iconImageNameSuffix;
    }

    public boolean isEnabled() { return this.enabled; }

    public String getUrlToLoad() { return this.urlToLoad; }
}
