package org.septa.android.app.database.update;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.septa.android.app.MainActivity;
import org.septa.android.app.R;
import org.septa.android.app.database.DatabaseManager;
import org.septa.android.app.support.CrashlyticsManager;
import org.septa.android.app.support.CursorAdapterSupplier;
import org.septa.android.app.support.GeneralUtils;

import java.io.File;
import java.util.Arrays;

public abstract class DatabaseUpgradeUtils {

    private static String TAG = DatabaseUpgradeUtils.class.getSimpleName();

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 121915;

    public static void checkForNewDatabase(Context context) {
        CheckForLatestDB.CheckForLatestDBListener listener;
        if (context instanceof CheckForLatestDB.CheckForLatestDBListener) {
            listener = (CheckForLatestDB.CheckForLatestDBListener) context;
        } else {
            IllegalArgumentException iae = new IllegalArgumentException("Context Must Implement CheckForLatestDBListener");
            CrashlyticsManager.log(Log.ERROR, TAG, iae.toString());
            throw iae;
        }

        if (GeneralUtils.isConnectedToInternet(context)) {
            CheckForLatestDB checkForLatestDB = new CheckForLatestDB(listener);
            checkForLatestDB.execute();
        } else {
            // do nothing -- do not tell user there may be a new DB available
            Log.d(TAG, "No network connection established -- cannot check for new DB");
        }
    }

    public static void prepareForNewDatabase(Context context) {
        // set up listeners
        ExpandDBZip.ExpandDBZipListener zipListener;
        if (context instanceof ExpandDBZip.ExpandDBZipListener) {
            zipListener = (ExpandDBZip.ExpandDBZipListener) context;
        } else {
            IllegalArgumentException iae = new IllegalArgumentException("Context Must Implement ExpandDBZipListener");
            CrashlyticsManager.log(Log.ERROR, TAG, iae.toString());
            throw iae;
        }

        CleanOldDB.CleanOldDBListener cleanListener;
        if (context instanceof CleanOldDB.CleanOldDBListener) {
            cleanListener = (CleanOldDB.CleanOldDBListener) context;
        } else {
            IllegalArgumentException iae = new IllegalArgumentException("Context Must Implement CleanOldDBListener");
            CrashlyticsManager.log(Log.ERROR, TAG, iae.toString());
            throw iae;
        }

        // get DB versionDownloaded and versionInstalled number
        int versionDownloaded = DatabaseSharedPrefsUtils.getVersionDownloaded(context);
        int versionInstalled = DatabaseSharedPrefsUtils.getVersionInstalled(context);
        int currentDBVersion = DatabaseManager.getDatabase(context).getVersion();
        boolean areThereFilesToClean = DatabaseSharedPrefsUtils.getNeedToClean(context);

        // install new DB if not done already
        if (versionDownloaded > versionInstalled) {
            // get downloaded file from databases directory
            String newDatabaseZipFilename = new StringBuilder("SEPTA_").append(versionDownloaded).append("_sqlite.zip").toString();
            File newDbZip = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), newDatabaseZipFilename);

            // validate that zip file has finished downloading
            if (newDbZip.isFile() && newDbZip.exists()) {
                // expand DB on a background thread
                ExpandDBZip expandDBZip = new ExpandDBZip(context, zipListener, newDbZip, versionDownloaded);
                expandDBZip.execute();
            } else {
                // redo download (it may have been interrupted)
                if (GeneralUtils.isConnectedToInternet(context)) {
                    // user already granted permission and download was cancelled by user
                    // download it without asking for permission again
                    DatabaseUpgradeUtils.downloadNewDatabase(context);
                } else {
                    // do nothing -- do not tell user there may be a new DB available
                    Log.d(TAG, "No network connection established -- cannot check for new DB");
                }
            }
        } else if (areThereFilesToClean && versionInstalled == currentDBVersion // app has been restarted
                && (versionDownloaded == versionInstalled       // DB installed from download and ready for use
                || currentDBVersion > versionDownloaded)) {     // DB in-app is newer than one on API
            // remove old DB files
            CleanOldDB cleanOldDB = new CleanOldDB(context, cleanListener, versionInstalled);
            cleanOldDB.execute();
        } else if (versionDownloaded == versionInstalled && versionInstalled != currentDBVersion) {
            // notify new database ready if not already using most up to date version of database
            cleanListener.notifyNewDatabaseReady();
        }
    }

    public static boolean decideWhetherToAskToDownload(Context context, int latestDBVersion, String latestDBURL, String updatedDate) {
        // save latest DB version and URL
        DatabaseSharedPrefsUtils.setLatestVersionAvailable(context, latestDBVersion);
        DatabaseSharedPrefsUtils.setLatestDownloadUrl(context, latestDBURL);

        // check API for DB version number
        int currentDBVersion = DatabaseManager.getDatabase(context).getVersion();
        int versionDownloaded = DatabaseSharedPrefsUtils.getVersionDownloaded(context);

        Log.d(TAG, "Latest DB Version: " + latestDBVersion + " vs. Old DB Version: " + currentDBVersion);

        // check if newer database available
        if (latestDBVersion > currentDBVersion && latestDBVersion > versionDownloaded) {

            // check if permission granted previously
            return !DatabaseSharedPrefsUtils.getPermissionToDownload(context);
        }

        return false;
    }

    public static AlertDialog promptToDownload(final Context context) {
        String downloadNow = context.getString(R.string.prompt_download_button_positive_download_now),
                remindMeLater = context.getString(R.string.prompt_download_button_negative_remind_later),
                goToNTA = context.getString(R.string.prompt_download_button_neutral_nta);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            downloadNow = downloadNow.replace("\n", "");
            remindMeLater = remindMeLater.replace("\n", "");
            goToNTA = goToNTA.replace("\n", "");
        }

        final AlertDialog dialog = new AlertDialog.Builder(context).setCancelable(true).setTitle(R.string.prompt_download_database_title)
                .setMessage(R.string.prompt_download_database_description)

                // approved download
                .setPositiveButton(downloadNow, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // ask for run time permission if running sdk 23+
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                                context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            // need user permission

                            if (((MainActivity) context).shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                // can explain why permissions needed here
                            }

                            // ask for permission
                            ((MainActivity) context).requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        } else {
                            // permission granted to start download
                            DatabaseUpgradeUtils.downloadNewDatabase(context);
                        }
                    }
                })

                // remind of download later
                .setNegativeButton(remindMeLater, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })

                // link to NTA
                .setNeutralButton(goToNTA, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ((MainActivity) context).switchToNextToArrive();
                    }
                })
                .create();

        // set "download now" button color
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
            }
        });

        return dialog;
    }

    public static void permissionResponseReceived(Context context, int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission granted to start database download
                DatabaseUpgradeUtils.downloadNewDatabase(context);
            } else {
                // user refused to grant permission
                Toast.makeText(context, R.string.download_permission_needed, Toast.LENGTH_LONG).show();
            }
        }
    }

    private static void downloadNewDatabase(Context context) {
        DownloadNewDB.DownloadNewDBListener listener;
        if (context instanceof DownloadNewDB.DownloadNewDBListener) {
            listener = (DownloadNewDB.DownloadNewDBListener) context;
        } else {
            IllegalArgumentException iae = new IllegalArgumentException("Context Must Implement DownloadNewDBListener");
            CrashlyticsManager.log(Log.ERROR, TAG, iae.toString());
            throw iae;
        }

        int currentDBVersion = DatabaseManager.getDatabase(context).getVersion();
        int latestDBVersion = DatabaseSharedPrefsUtils.getLatestVersionAvailable(context);
        String latestDBURL = DatabaseSharedPrefsUtils.getLatestDownloadUrl(context);

        // download new DB zip from url given by API
        if (latestDBVersion > currentDBVersion && !latestDBURL.isEmpty()) {
            // save permission to download
            DatabaseSharedPrefsUtils.setPermissionToDownload(context, true);

            // check if download already in progress
            if (!isDownloading(context)) {
                // overwrite any other download listeners
                listener.clearCorruptedDownloadRefId();
                DatabaseSharedPrefsUtils.clearDownloadRefId(context);

                // do not need to recheck for connection -- handled by DownloadManager
                DownloadNewDB downloadNewDB = new DownloadNewDB(context, listener, latestDBURL, latestDBVersion);
                downloadNewDB.execute();
            } else {
                Log.d(TAG, "DB download already in progress");
            }
        } else {
            Log.e(TAG, "Could not download new DB with version: " + latestDBVersion + " from URL: " + latestDBURL);
        }
    }

    private static boolean isDownloading(Context context) {
        int downloadStatus = getDownloadStatus(context, DatabaseSharedPrefsUtils.getDownloadRefId(context));
        int[] inProgressStatuses = new int[]{DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PENDING, DownloadManager.STATUS_PAUSED, DownloadManager.PAUSED_WAITING_TO_RETRY, DownloadManager.PAUSED_WAITING_FOR_NETWORK, DownloadManager.PAUSED_QUEUED_FOR_WIFI};
        return Arrays.asList(inProgressStatuses).contains(downloadStatus);
    }

    private static int getDownloadStatus(Context context, long downloadId) {
        DownloadManager downloadManager =
                (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId); // filter your download by download Id
        Cursor c;
        if (downloadManager != null) {
            c = downloadManager.query(query);
            if (c.moveToFirst()) {
                int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                c.close();
                Log.i("DOWNLOAD_STATUS", String.valueOf(status));
                return status;
            }
        }
        Log.i("AUTOMATION_DOWNLOAD", "DEFAULT");
        return -1;
    }

    public static void saveDownloadedVersionNumber(Context context, long downloadRefId, int version) {        // add to list of downloads
        DatabaseSharedPrefsUtils.saveDownloadRefId(context, downloadRefId);

        // save new version # to shared preferences
        DatabaseSharedPrefsUtils.setVersionDownloaded(context, version);

        // this should handle receiving finished downloads that were interrupted
        // DownloadManager handles resuming / finishing the downloads
        Log.d(TAG, "Saving DB download ref ID: " + downloadRefId);
    }

    private static boolean validateDatabaseVersion(Context context) {
        // validate new database is correct version by comparing version in table and version downloaded
        int versionDownloaded = DatabaseSharedPrefsUtils.getVersionDownloaded(context);

        // get this from looking at DB table dbVersion
        CursorAdapterSupplier<Integer> cursorAdapterSupplier = DatabaseManager.getInstance(context).getVersionOfDatabase();
        Cursor cursor = cursorAdapterSupplier.getCursor(context, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                Integer unzippedDbVersion = cursorAdapterSupplier.getCurrentItemFromCursor(cursor);
                Log.d(TAG, "Unzipped DB Version: " + unzippedDbVersion);

                // version of unzipped DB should match the downloaded version
                return versionDownloaded == unzippedDbVersion;
            }
            cursor.close();
        } else {
            Log.e(TAG, "Error finding dbVersion in unzipped database");
        }

        return false;
    }

    public static void saveNewDatabaseReady(Context context, int versionInstalled) {
        ExpandDBZip.ExpandDBZipListener listener;
        if (context instanceof ExpandDBZip.ExpandDBZipListener) {
            listener = (ExpandDBZip.ExpandDBZipListener) context;
        } else {
            IllegalArgumentException iae = new IllegalArgumentException("Context Must Implement ExpandDBZipListener");
            CrashlyticsManager.log(Log.ERROR, TAG, iae.toString());
            throw iae;
        }

        if (validateDatabaseVersion(context)) {
            // save new versionInstalled number in shared pref
            DatabaseSharedPrefsUtils.setVersionInstalled(context, versionInstalled);
            DatabaseSharedPrefsUtils.setDatabaseFilename(context, new StringBuilder("SEPTA_").append(versionInstalled).append(".sqlite").toString());

            // restore download permission back to false
            DatabaseSharedPrefsUtils.setPermissionToDownload(context, false);
        } else {
            Log.e(TAG, "Unzipped DB version did not match DB version downloaded -- removing corrupt file");
            listener.clearCorruptedDownloadRefId();
            DatabaseSharedPrefsUtils.clearDownloadRefId(context);
        }
    }

    public static AlertDialog buildNewDatabaseReadyPopUp(final Context context) {
        int versionDownloaded = DatabaseSharedPrefsUtils.getVersionDownloaded(context);
        int versionInstalled = DatabaseSharedPrefsUtils.getVersionInstalled(context);
        int versionInUse = DatabaseManager.getDatabase(context).getVersion();

        // validate using older DB version but new version is ready
        if (versionDownloaded == versionInstalled && versionInstalled > versionInUse) {
            // remember that old DB files need to be cleaned
            DatabaseSharedPrefsUtils.setNeedToClean(context, true);

            // notify user that new database ready for use
            final AlertDialog dialog = new AlertDialog.Builder(context).setCancelable(true).setTitle(R.string.prompt_use_new_database_title)
                    .setMessage(R.string.prompt_use_new_database_description)
                    .setPositiveButton(R.string.prompt_use_new_database_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // set up new database
                            setUpNewDatabase(context);
                        }
                    })
                    .create();

            return dialog;
        } else {
            Log.e(TAG, "New DB Version Not Yet Ready. Downloaded: " + versionDownloaded + " Installed: " + versionInstalled + " In Use: " + versionInUse);
        }
        return null;
    }

    private static void setUpNewDatabase(Context context) {
        CleanOldDB.CleanOldDBListener cleanListener;
        if (context instanceof CleanOldDB.CleanOldDBListener) {
            cleanListener = (CleanOldDB.CleanOldDBListener) context;
        } else {
            IllegalArgumentException iae = new IllegalArgumentException("Context Must Implement CleanOldDBListener");
            CrashlyticsManager.log(Log.ERROR, TAG, iae.toString());
            throw iae;
        }

        // initialize new DB
        DatabaseManager.reinitDatabase(context);

        // remove old DB files
        CleanOldDB cleanOldDB = new CleanOldDB(context, cleanListener, DatabaseSharedPrefsUtils.getVersionInstalled(context));
        cleanOldDB.execute();
    }

    public static void databaseUpdateComplete(Context context) {
        // no need to clean DB files
        DatabaseSharedPrefsUtils.setNeedToClean(context, false);

        // notify user that database update complete
        Toast.makeText(context, R.string.notification_database_updated, Toast.LENGTH_SHORT).show();
    }
}
