/*
 * EmailLaunch.java
 * Last modified on 02-06-2014 15:08-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.utilities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import org.septa.android.app.R;

public class EmailLaunch {
    private static final String TAG = EmailLaunch.class.getName();

    public static void launchEmail(Context context, String sendToEmailAddress, String subject, String emailBodyText, Uri imageUri) {
        Intent sendEmailIntent = new Intent(Intent.ACTION_SENDTO);
        sendEmailIntent.setType("image/jpeg");
        sendEmailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{sendToEmailAddress});
        sendEmailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sendEmailIntent.putExtra(Intent.EXTRA_TEXT, emailBodyText);
        if (imageUri != null) {
            sendEmailIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        }
         try {

            context.startActivity(Intent.createChooser(sendEmailIntent, "Send feedback mail..."));
        } catch (android.content.ActivityNotFoundException ex) {

            Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
