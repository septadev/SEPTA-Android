/*
 * FormSubmissionHelper.java
 * Last modified on 02-14-2014 15:03-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.utilities;

import android.util.Log;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.septa.android.app.activities.CommentsFormActionBarActivity;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class FormSubmissionHelper {
    public static final String TAG = FormSubmissionHelper.class.getName();

    public static UrlEncodedFormEntity postReadyFormNameValuesEntity(List nameValuePairs) {
        UrlEncodedFormEntity urlEncodedFormEntity = null;

        try {
            urlEncodedFormEntity = new UrlEncodedFormEntity(nameValuePairs);
        } catch (UnsupportedEncodingException usee) {
            Log.e(TAG, "unsupported encoding exception");
        }

        return urlEncodedFormEntity;
    }

}
