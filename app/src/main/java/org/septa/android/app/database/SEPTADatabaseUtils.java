package org.septa.android.app.database;

import android.content.Context;

public class SEPTADatabaseUtils {

    // database shared preferences
    private static final String SHARED_PREFERENCES_DATABASE = "SHARED_PREFERENCES_DATABASE";
    private static final String NEWEST_DB_VERSION_DOWNLOADED = "NEWEST_DB_VERSION_DOWNLOADED";
    private static final String NEWEST_DB_VERSION_INSTALLED = "NEWEST_DB_VERSION_INSTALLED";
    private static final String PERMISSION_TO_DOWNLOAD = "PERMISSION_TO_DOWNLOAD";
    private static final String DB_FILENAME = "DB_FILENAME";
    private static final String DOWNLOAD_REF_ID = "DOWNLOAD_REF_ID";
    private static final String NEED_DB_CLEANING = "NEED_DB_CLEANING";
    private static final String NEED_RESTART = "NEED_RESTART";

    public static int getVersionDownloaded(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_DATABASE, Context.MODE_PRIVATE).getInt(NEWEST_DB_VERSION_DOWNLOADED, SEPTADatabase.getDatabaseVersion());
    }

    public static void setVersionDownloaded(Context context, int versionDownloaded) {
        context.getSharedPreferences(SHARED_PREFERENCES_DATABASE, Context.MODE_PRIVATE).edit().putInt(NEWEST_DB_VERSION_DOWNLOADED, versionDownloaded).apply();
    }

    public static int getVersionInstalled(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_DATABASE, Context.MODE_PRIVATE).getInt(NEWEST_DB_VERSION_INSTALLED, SEPTADatabase.getDatabaseVersion());
    }

    public static void setVersionInstalled(Context context, int versionDownloaded) {
        context.getSharedPreferences(SHARED_PREFERENCES_DATABASE, Context.MODE_PRIVATE).edit().putInt(NEWEST_DB_VERSION_INSTALLED, versionDownloaded).apply();
    }

    public static boolean getPermissionToDownload(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_DATABASE, Context.MODE_PRIVATE).getBoolean(PERMISSION_TO_DOWNLOAD, false);
    }

    public static void setPermissionToDownload(Context context, boolean permission) {
        context.getSharedPreferences(SHARED_PREFERENCES_DATABASE, Context.MODE_PRIVATE).edit().putBoolean(PERMISSION_TO_DOWNLOAD, permission).apply();
    }

    public static String getDatabaseFilename(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_DATABASE, Context.MODE_PRIVATE).getString(DB_FILENAME, SEPTADatabase.getDatabaseFileName());
    }

    public static void setDatabaseFilename(Context context, String filename) {
        context.getSharedPreferences(SHARED_PREFERENCES_DATABASE, Context.MODE_PRIVATE).edit().putString(DB_FILENAME, filename).apply();
    }

    public static long getDownloadRefId(Context context) {
        // TODO: default download ref ID??
        return context.getSharedPreferences(SHARED_PREFERENCES_DATABASE, Context.MODE_PRIVATE).getLong(DOWNLOAD_REF_ID, -1);
    }

    public static void clearDownloadRefId(Context context) {
        context.getSharedPreferences(SHARED_PREFERENCES_DATABASE, Context.MODE_PRIVATE).edit().remove(DOWNLOAD_REF_ID).apply();
    }

    public static void saveDownloadRefId(Context context, Long downloadRefId) {
        context.getSharedPreferences(SHARED_PREFERENCES_DATABASE, Context.MODE_PRIVATE).edit().putLong(DOWNLOAD_REF_ID, downloadRefId).apply();
    }

    public static boolean getNeedToClean(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_DATABASE, Context.MODE_PRIVATE).getBoolean(NEED_DB_CLEANING, false);
    }

    public static void setNeedToClean(Context context, boolean shouldClean) {
        context.getSharedPreferences(SHARED_PREFERENCES_DATABASE, Context.MODE_PRIVATE).edit().putBoolean(NEED_DB_CLEANING, shouldClean).apply();
    }

    public static boolean getNeedToRestart(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_DATABASE, Context.MODE_PRIVATE).getBoolean(NEED_RESTART, false);
    }

    public static void setNeedToRestart(Context context, boolean shouldRestart) {
        context.getSharedPreferences(SHARED_PREFERENCES_DATABASE, Context.MODE_PRIVATE).edit().putBoolean(NEED_RESTART, shouldRestart).apply();
    }
}
