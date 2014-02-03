/*
 * SocialPageLaunch.java
 * Last modified on 02-02-2014 19:34-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;

import org.septa.android.app.R;

public class SocialPageLaunch {
    private static final String TAG = SocialPageLaunch.class.getName();

    public static void facebook(Context context) {

        SocialPageLaunch.socialPageLaunch(context, context.getResources().getString(R.string.facebook));
    }

    public static void twitter(Context context) {

        SocialPageLaunch.socialPageLaunch(context, context.getResources().getString(R.string.twitter));
    }

    private static void socialPageLaunch(Context context, String socialApp) {
        Resources resources = context.getResources();

        int socialAppUriId = resources.getIdentifier("septa_" + socialApp.toLowerCase() + "_app_uri",
                "string", context.getPackageName());
        int socialHttpUriId = resources.getIdentifier("septa_" + socialApp.toLowerCase() + "_http_uri",
                "string", context.getPackageName());

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(resources.getString(socialAppUriId)));
            context.startActivity(intent);
        } catch (Exception ex) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(resources.getString(socialHttpUriId)));
            context.startActivity(intent);
        }
    }
}

