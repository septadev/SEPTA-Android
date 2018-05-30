package org.septa.android.app.database;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by mluansing on 9/20/17.
 */

public class CheckForLatestDB extends AsyncTask<Object, Object, Void> {

    // tag for logging purposes
    private static final String TAG = CheckForLatestDB.class.getSimpleName();

    private CheckForLatestDBListener mListener;
    Integer latestDBVersion;
    String latestDBURL, updatedDate;

    public CheckForLatestDB(CheckForLatestDBListener listener) {
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
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        // pass back latest DB metadata
        mListener.afterLatestDBMetadataLoad(latestDBVersion, latestDBURL, updatedDate);
    }

    public interface CheckForLatestDBListener {
        void afterLatestDBMetadataLoad(int latestDBVersion, String latestDBURL, String updatedDate);
    }

}
