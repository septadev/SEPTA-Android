package org.septa.android.app.database;

import android.content.Context;
import android.database.SQLException;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import org.septa.android.app.R;
import org.septa.android.app.support.CrashlyticsManager;
import org.septa.android.app.support.GeneralUtils;

public class SEPTADatabase extends SQLiteAssetHelper {
    private static String TAG = SEPTADatabase.class.getSimpleName();

    /**
     * Current packaged DB version, update number when packaged DB changes
     */
    private static final String LATEST_DATABASE_API_URL = "https://s3.amazonaws.com/mobiledb.septa.org/latest/latestDb.json";

    // these are left in case the user does not have the most up to date version of the database
    // modify databaseVersion and databaseFileName when pushing out a new release with a new DB
    private static int databaseVersion = 1;
    private static String databaseFileName = "SEPTA_1.sqlite";

    public SEPTADatabase(Context context, int databaseVersion, String databaseFileName) {
        super(context, databaseFileName, null, databaseVersion);

        setDatabaseVersion(databaseVersion);

        CrashlyticsManager.log(Log.INFO, TAG, "Initializing DB: " + databaseVersion);

        // Causes database to overwrite when version changes
        setForcedUpgrade();

        // create indices on new app database
        execSQL(new String[]{GeneralUtils.readRawTextFile(context, R.raw.db_init_script)});
    }

    private void execSQL(String statements[]) {
        try {
            for (String sql : statements) {
                getWritableDatabase().execSQL(sql);
            }
        } catch (SQLException e) {
            Log.e(TAG, e.toString());
        }
    }

    public static int getDatabaseVersion() {
        return databaseVersion;
    }

    private void setDatabaseVersion(int databaseVersion) {
        SEPTADatabase.databaseVersion = databaseVersion;

        String newDatabaseFilename = new StringBuilder("SEPTA_").append(databaseVersion).append(".sqlite").toString();
        setDatabaseFileName(newDatabaseFilename);
    }

    public static String getDatabaseFileName() {
        return databaseFileName;
    }

    private void setDatabaseFileName(String databaseFileName) {
        SEPTADatabase.databaseFileName = databaseFileName;
    }

    public static String getLatestDatabaseApiUrl() {
        return LATEST_DATABASE_API_URL;
    }
}