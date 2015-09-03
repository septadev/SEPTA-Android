package org.septa.android.app.PapalVisit;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.septa.android.app.BuildConfig;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by jhunchar on 9/3/15.
 */
public class PopeNetworkService extends IntentService {

    private static final String TAG = PopeNetworkService.class.getName();
    public static final String NOTIFICATION = "org.septa.android.app";

    public PopeNetworkService() {
        super("NetworkService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "onHandleIntent");
        }

        // Request message
        try {

            Message message = BuildConfig.DEBUG ? PopeRestClient.get().getDebugMessage() : PopeRestClient.get().getMessage();

            success(message, null);
        }

        catch (RetrofitError retrofitError) {
            failure(retrofitError);
        }
    }

    public void success(Message message, Response response) {

        // Response was successful
        // Show results
        if (message != null) {
            handleResult(PopeConstants.VALUE_SUCCESS, message);
        }

        // No content available, but there always should be
        // Show error view
        else {
            handleResult(PopeConstants.VALUE_ERROR, null);
        }
    }

    public void failure(RetrofitError error) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "failure");
        }

        // Network error
        // Show error view
        handleResult(PopeConstants.VALUE_ERROR, null);
    }

    private void handleResult(String result, Object object) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "handleResult: " + result);
        }

        // Broadcast result
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(PopeConstants.KEY_RESULT, result);

        if (object != null) {
            String forecastJson = GsonObject.convertObjectToJsonString(object, false);
            intent.putExtra(PopeConstants.KEY_JSON_RESPONSE, forecastJson);
        }

        sendBroadcast(intent);
    }
}
