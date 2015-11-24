/*
 * SEPTADatabase.java
 * Last modified on 04-21-2014 13:03-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.databases;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import org.septa.android.app.R;

public class SEPTADatabase extends SQLiteAssetHelper {
    public static final String TAG = SEPTADatabase.class.getName();

    /**
     * Current packaged DB version, update number when packaged DB changes
     */
    private static final int DATABASE_VERSION = 30;

    public SEPTADatabase(Context context) {

        super(context, context.getString(R.string.SEPTA_database_filename), null, DATABASE_VERSION);
        // Causes database to overwrite when version changes
        setForcedUpgrade();
    }
}