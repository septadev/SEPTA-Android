package org.septa.android.app.database.update;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.septa.android.app.database.DatabaseManager;
import org.septa.android.app.database.SEPTADatabase;

public class TempDatabaseManager {

    public static final String TAG = DatabaseManager.class.getSimpleName();

    public static SQLiteDatabase getDatabaseWithVersion(Context context, int version) {
        return new SEPTADatabase(context, version, "SEPTA_" + version + ".sqlite").getReadableDatabase();
    }
}
