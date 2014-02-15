/*
 * CommentsFormSubmissionStrategy.java
 * Last modified on 02-15-2014 11:23-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.strategies;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.squareup.okhttp.apache.OkApacheClient;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.septa.android.app.R;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class CommentsFormSubmissionStrategy extends AsyncTask<List<NameValuePair>, Integer, Double> {
    final static String TAG = CommentsFormSubmissionStrategy.class.getName();

    final Context context;

    public CommentsFormSubmissionStrategy(Context inContext) {

        this.context = inContext;
    }

    @Override
    protected Double doInBackground(List<NameValuePair>... params) {
        Log.d(TAG, "doInBackground being called");
        postData(params[0]);

        return null;
    }

    protected void onPostExecute(Double result){

        Toast.makeText(context.getApplicationContext(), "form submission is complete.", Toast.LENGTH_LONG).show();
    }
    protected void onProgressUpdate(Integer... progress){
        Log.d(TAG, "onProgressUpdate with Integer of " + progress[0]);
    }

    public void postData(List<NameValuePair> nameValuePairsToPost) {
        OkApacheClient httpClient = new OkApacheClient();
        URI commentsFormPostURI = null;

        String urlString = context.getString(R.string.network_base_septa_org_url_protocol)+
                "://"+
                context.getString(R.string.network_base_septa_org_url)+
                context.getString(R.string.commentsForm_submitPath);

        Log.d(TAG, "the url to send to is "+urlString);
        try {

            commentsFormPostURI = new URI(urlString);
        } catch (URISyntaxException urise) {

            Log.e(TAG, "URI syntax exception");
        }

        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(urlString);
        HttpResponse response = null;

        try {
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairsToPost));

            // Execute HTTP Post Request
            response = httpclient.execute(httppost);
        } catch (ClientProtocolException e) {

            Log.e(TAG, "client protocol exception");
        } catch (IOException e) {

            Log.e(TAG, "IOException found");
        }

        Log.d(TAG, "result? " + response.getStatusLine().getStatusCode());
    }
}
