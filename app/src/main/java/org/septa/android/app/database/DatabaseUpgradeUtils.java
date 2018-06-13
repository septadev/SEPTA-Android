package org.septa.android.app.database;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.septa.android.app.MainActivity;
import org.septa.android.app.R;
import org.septa.android.app.SplashScreenActivity;
import org.septa.android.app.support.CrashlyticsManager;
import org.septa.android.app.support.GeneralUtils;

import java.io.File;
import java.util.Arrays;

public class DatabaseUpgradeUtils {

    private static String TAG = DatabaseUpgradeUtils.class.getSimpleName();

    private static final int DELAY_TO_RESTART = 500,
            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 121915;

    // this must be a unique ID for the schedule update notif
    // ensure that ID will not clash with push notifs IDs
    private static final int SCHEDULE_UPDATE_NOTIF_ID = 1219;

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
        int versionDownloaded = SEPTADatabaseUtils.getVersionDownloaded(context);
        int versionInstalled = SEPTADatabaseUtils.getVersionInstalled(context);
        int currentDBVersion = SEPTADatabase.getDatabaseVersion();
        boolean areThereFilesToClean = SEPTADatabaseUtils.getNeedToClean(context);

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
        } else if (versionDownloaded == versionInstalled) {
            // only prompt to restart if not already using most up to date version of database
            promptToRestart(context);
        }
    }

    public static boolean decideWhetherToAskToDownload(Context context, int latestDBVersion, String latestDBURL, String updatedDate) {
        // save latest DB version and URL
        SEPTADatabaseUtils.setLatestVersionAvailable(context, latestDBVersion);
        SEPTADatabaseUtils.setLatestDownloadUrl(context, latestDBURL);

        // check API for DB version number
        int currentDBVersion = SEPTADatabase.getDatabaseVersion();
        int versionDownloaded = SEPTADatabaseUtils.getVersionDownloaded(context);

        Log.d(TAG, "Latest DB Version: " + latestDBVersion + " vs. Old DB Version: " + currentDBVersion);

        // check if newer database available
        if (latestDBVersion > currentDBVersion && latestDBVersion > versionDownloaded) {

            // check if permission granted previously
            return !SEPTADatabaseUtils.getPermissionToDownload(context);
        }

        return false;
    }

    public static AlertDialog promptToDownload(final Context context) {
        final AlertDialog dialog = new AlertDialog.Builder(context).setCancelable(true).setTitle(R.string.prompt_download_database_title)
                .setMessage(R.string.prompt_download_database_description)

                // approved download
                .setPositiveButton(R.string.prompt_download_button_positive_download_now, new DialogInterface.OnClickListener() {
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
                .setNegativeButton(R.string.prompt_download_button_negative_remind_later, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })

                // link to NTA
                .setNeutralButton(R.string.prompt_download_button_neutral_nta, new DialogInterface.OnClickListener() {
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

        int currentDBVersion = SEPTADatabase.getDatabaseVersion();
        int latestDBVersion = SEPTADatabaseUtils.getLatestVersionAvailable(context);
        String latestDBURL = SEPTADatabaseUtils.getLatestDownloadUrl(context);

        // download new DB zip from url given by API
        if (latestDBVersion > currentDBVersion && !latestDBURL.isEmpty()) {
            // save permission to download
            SEPTADatabaseUtils.setPermissionToDownload(context, true);

            // check if download already in progress
            if (!isDownloading(context)) {
                // overwrite any other download listeners
                listener.clearCorruptedDownloadRefId();
                SEPTADatabaseUtils.clearDownloadRefId(context);

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
        int downloadStatus = getDownloadStatus(context, SEPTADatabaseUtils.getDownloadRefId(context));
        int[] inProgressStatuses = new int[]{DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PENDING, DownloadManager.STATUS_PAUSED, DownloadManager.PAUSED_WAITING_TO_RETRY, DownloadManager.PAUSED_WAITING_FOR_NETWORK, DownloadManager.PAUSED_QUEUED_FOR_WIFI};
        return Arrays.asList(inProgressStatuses).contains(downloadStatus);
    }

    private static int getDownloadStatus(Context context , long downloadId) {
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
        SEPTADatabaseUtils.saveDownloadRefId(context, downloadRefId);

        // save new version # to shared preferences
        SEPTADatabaseUtils.setVersionDownloaded(context, version);
        // should not need to check if already downloaded -- this is used for unzipping the correct file

        // this should handle receiving finished downloads that were interrupted
        // DownloadManager handles resuming / finishing the downloads
        Log.d(TAG, "Saving DB download ref ID: " + downloadRefId);
    }

    public static void notifyNewDatabaseReady(Context context, int versionInstalled) {
        // save new versionInstalled number in shared pref
        SEPTADatabaseUtils.setVersionInstalled(context, versionInstalled);
        SEPTADatabaseUtils.setDatabaseFilename(context, new StringBuilder("SEPTA_").append(versionInstalled).append(".sqlite").toString());

        // restore download permission back to false
        SEPTADatabaseUtils.setPermissionToDownload(context, false);

        // show notification that download completed
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.notification_database_download_complete));

        // set up notification intent to restart app on click
        Intent notificationIntent = new Intent(context, SplashScreenActivity.class);

        // force full restart of application on notification click
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        SEPTADatabaseUtils.setNeedToRestart(context, true);
        PendingIntent restartIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(restartIntent).setAutoCancel(true);

        // send notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(SCHEDULE_UPDATE_NOTIF_ID, mBuilder.build());
        } else {
            Log.e(TAG, "Could not send notification that new DB is ready");
        }
    }

    public static AlertDialog promptToRestart(final Context context) {
        int versionDownloaded = SEPTADatabaseUtils.getVersionDownloaded(context);
        int versionInstalled = SEPTADatabaseUtils.getVersionInstalled(context);

        if (versionDownloaded == versionInstalled && versionInstalled > SEPTADatabase.getDatabaseVersion()) {
            // remember that old DB files need to be cleaned
            SEPTADatabaseUtils.setNeedToClean(context, true);

            // prompt user to restart app
            final AlertDialog dialog = new AlertDialog.Builder(context).setCancelable(true).setTitle(R.string.prompt_restart_database_title)
                    .setMessage(R.string.prompt_restart_database_description)

                    // approved restart
                    .setPositiveButton(R.string.prompt_restart_button_positive_now, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // restart app
                            restartApplication(context);
                        }
                    })

                    // remind of download later
                    .setNegativeButton(R.string.prompt_restart_button_negative_later, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();

            // set "restart now" button color
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
                }
            });

            return dialog;
        }
        return null;
    }

    private static void restartApplication(Context context) {
        // restart the app
        Intent restartIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        restartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent intent = PendingIntent.getActivity(context, 0, restartIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.set(AlarmManager.RTC, System.currentTimeMillis() + DELAY_TO_RESTART, intent);
        System.exit(2);
    }

    public static void databaseUpdateComplete(Context context) {
        // no need to clean DB files
        SEPTADatabaseUtils.setNeedToClean(context, false);

        // dismiss schedule restart notif
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(SCHEDULE_UPDATE_NOTIF_ID);

        // notify user that database update complete
        Toast.makeText(context, R.string.notification_database_updated, Toast.LENGTH_SHORT).show();
    }
}
