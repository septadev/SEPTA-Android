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

    public static Intent launchEmail(Context context, String sendToEmailAddress, String subject, String emailBodyText, Uri imageUri) {
        String uriString = context.getString(R.string.mailto_protocol_string)+sendToEmailAddress+
                           context.getString(R.string.mailto_subject_uriString)+subject+
                           context.getString(R.string.mailto_body_uriString)+emailBodyText;
        Uri emailUri = Uri.parse(uriString);

        Intent sendEmailIntent = new Intent(Intent.ACTION_SENDTO, emailUri);

        if (imageUri != null) {

            sendEmailIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        }

        return sendEmailIntent;

//        try {
//
//            context.startActivity(Intent.createChooser(sendEmailIntent, context.getString(R.string.mailto_chooser_text)));
//        } catch (android.content.ActivityNotFoundException ex) {
//
//            Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
//        }
    }
}
