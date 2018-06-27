package org.septa.android.app.database.update;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;

public class CleanOldDB extends AsyncTask<Object, Object, Void> {

    // tag for logging purposes
    private static final String TAG = CleanOldDB.class.getSimpleName();

    private Context context;
    private CleanOldDBListener mListener;
    private int version;

    CleanOldDB(Context context, CleanOldDBListener listener, int version) {
        this.context = context;
        this.mListener = listener;
        this.version = version;
    }

    @Override
    protected Void doInBackground(Object... voids) {
        // look at directory of current files and find any databases that aren't newDbVersionToUse
        // -- SEPTA.sqlite is legacy filename, starting after 282 version numbers will be added in filenames
        // SEPTA_14_sqlite.zip (externaL) and SEPTA_14.sqlite (internal)
        // delete old copies of '<DBNAME>-journal' like '<DBNAME>' ex. SEPTA_14.sqlite and SEPTA_14.sqlite-journal
        // journal file is made when db started up for the first time

        final File internalDir = new File(new File(context.getApplicationInfo().dataDir), "databases");

        // clean up old database files
        for (File fileToDelete : internalDir.listFiles()) {
            String newDatabaseFilename = new StringBuilder("SEPTA_").append(version).append(".sqlite").toString();
            String fileToDeleteName = fileToDelete.getName();

            // delete internal files unrelated to current database
            if (!(fileToDeleteName.contains(newDatabaseFilename))) {

                // only delete files related to DB
                if (fileToDeleteName.contains(".sqlite")) {
                    // SEPTA.sqlite, SEPTA.sqlite.zip, SEPTA.sqlite-journal

                    Log.d(TAG, "Deleting DB file " + fileToDeleteName);
                    if (!fileToDelete.delete()) {
                        Log.e(TAG, "Unable to delete DB file " + fileToDeleteName);
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        // notify activity that old DB files cleaned
        mListener.afterOldDBCleaned();
    }

    public interface CleanOldDBListener {
        void afterOldDBCleaned();
        void notifyNewDatabaseReady();
    }

}
