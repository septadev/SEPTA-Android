package org.septa.android.app.database;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class SEPTADatabase extends SQLiteAssetHelper {

    /**
     * Current packaged DB version, update number when packaged DB changes
     */
    private static final int DATABASE_VERSION = 70;
    private static final String DATABASE_FILE_NAME = "SEPTA.sqlite";

    public SEPTADatabase(Context context) {

        super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION);
        // Causes database to overwrite when version changes
        setForcedUpgrade();
    }
}