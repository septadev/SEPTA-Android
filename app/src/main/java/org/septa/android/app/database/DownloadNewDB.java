package org.septa.android.app.database;

import android.app.DownloadManager;
import android.net.Uri;
import android.os.AsyncTask;

/**
 * Created by mluansing on 9/20/17.
 */

public class DownloadNewDB extends AsyncTask<Object, Object, Void> {

    private DownloadManager.Request request;
    private DownloadNewDBListener mListener;
    private Uri url;

    // tag for logging purposes
    private static final String TAG = DownloadNewDB.class.getSimpleName();

    public DownloadNewDB(DownloadNewDBListener listener, String url) {
        this.mListener = listener;

        // validate URL
        this.url = Uri.parse(url);
    }

    @Override
    protected Void doInBackground(Object... voids) {
        request = new DownloadManager.Request(url)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle("SEPTA")
                .setDescription("Downloading the latest schedule...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);

        // TODO: android.permission.DOWNLOAD_WITHOUT_NOTIFICATION needed if set to VISIBILITY_HIDDEN

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        // pass result back to activity
        mListener.afterNewDBDownload(request);
    }

    public interface DownloadNewDBListener {
        void afterNewDBDownload(DownloadManager.Request request);
//        void updateDBDownloadProgress(double progress);
    }

}
