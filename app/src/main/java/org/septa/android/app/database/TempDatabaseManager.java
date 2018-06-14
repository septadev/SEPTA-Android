package org.septa.android.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

class TempDatabaseManager {

    public static final String TAG = DatabaseManager.class.getSimpleName();

    public static SQLiteDatabase getDatabaseWithVersion(Context context, int version) {
        return new SEPTADatabase(context, version, "SEPTA_" + version + ".sqlite").getReadableDatabase();
    }
}
