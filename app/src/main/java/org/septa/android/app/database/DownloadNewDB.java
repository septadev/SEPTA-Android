package org.septa.android.app.database;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

/**
 * Created by mluansing on 9/20/17.
 */

public class DownloadNewDB extends AsyncTask<Object, Object, Void> {

    private DownloadManager.Request request;
    private Context context;
    private DownloadNewDBListener mListener;
    private Uri url;
    private int version;

    // tag for logging purposes
    private static final String TAG = DownloadNewDB.class.getSimpleName();

    public DownloadNewDB(Context context, DownloadNewDBListener listener, String url, int version) {
        this.context = context;
        this.mListener = listener;
        this.version = version;

        // validate URL
        this.url = Uri.parse(url);
    }

    @Override
    protected Void doInBackground(Object... voids) {
        String filename = new StringBuilder("SEPTA_").append(version).append("_sqlite.zip").toString();

        request = new DownloadManager.Request(url)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle("SEPTA")
                .setDescription("Downloading the latest schedule...")
                // show visible only during download
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                // download zip to external storage
                .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, filename);

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        // pass result back to activity
        mListener.afterNewDBDownload(request, version);
    }

    public interface DownloadNewDBListener {
        void afterNewDBDownload(DownloadManager.Request request, int version);
//        void updateDBDownloadProgress(double progress);
    }

}
