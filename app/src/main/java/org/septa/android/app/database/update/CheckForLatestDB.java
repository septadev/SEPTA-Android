package org.septa.android.app.database.update;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.septa.android.app.database.SEPTADatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class CheckForLatestDB extends AsyncTask<Object, Object, Void> {

    // tag for logging purposes
    private static final String TAG = CheckForLatestDB.class.getSimpleName();

    private CheckForLatestDBListener mListener;
    private Integer latestDBVersion;
    private String latestDBURL, updatedDate;

    CheckForLatestDB(CheckForLatestDBListener listener) {
        this.mListener = listener;
    }

    @Override
    protected Void doInBackground(Object... voids) {
        try {
            // connect to URL
            URL jsonDataUrl = new URL(SEPTADatabase.getLatestDatabaseApiUrl());
            HttpURLConnection httpURLConnection = (HttpURLConnection) jsonDataUrl.openConnection();

            // read JSON from URL
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuffer stringBuffer = new StringBuffer();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            // grab latest DB metadata
            JSONObject latestDBMetadata = new JSONObject(stringBuffer.toString());
            latestDBVersion = latestDBMetadata.getInt("version");
            latestDBURL = latestDBMetadata.getString("url");
            updatedDate = latestDBMetadata.getString("updateDate");
        } catch (MalformedURLException e) {
            Log.e(TAG, e.toString());
            cancel(true);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            cancel(true);
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            cancel(true);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (isCancelled()) {
            return;
        }
        // pass back latest DB metadata
        mListener.afterLatestDBMetadataLoad(latestDBVersion, latestDBURL, updatedDate);
    }

    public interface CheckForLatestDBListener {
        void afterLatestDBMetadataLoad(int latestDBVersion, String latestDBURL, String updatedDate);
    }

}
