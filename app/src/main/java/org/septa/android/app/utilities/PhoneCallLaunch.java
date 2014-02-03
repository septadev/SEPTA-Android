/*
 * PhoneCallLaunch.java
 * Last modified on 02-03-2014 10:37-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.utilities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.septa.android.app.R;

public class PhoneCallLaunch {
    private static final String TAG = PhoneCallLaunch.class.getName();

    public static void launchPhoneCall(Context context, String phoneNumber) {
        try {
            String phoneNumberUri = context.getString(R.string.telephone_number_prefix) + phoneNumber;

            Intent launchPhoneCallIntent = new Intent(Intent.ACTION_CALL);
            launchPhoneCallIntent.setData(Uri.parse(phoneNumberUri));

            context.startActivity(launchPhoneCallIntent);
        } catch (Exception e) {

            Log.e(TAG, "Failed to invoke call", e);
        }
    }
}
