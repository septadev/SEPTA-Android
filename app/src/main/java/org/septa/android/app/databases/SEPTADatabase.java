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

    private static final int DATABASE_VERSION = 1;

    public SEPTADatabase(Context context) {

        super(context, context.getString(R.string.SEPTA_database_filename), null, DATABASE_VERSION);
    }
}