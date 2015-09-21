package org.septa.android.app.events.network;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.septa.android.app.BuildConfig;
import org.septa.android.app.events.EventsConstants;
import org.septa.android.app.events.model.GsonObject;
import org.septa.android.app.events.model.Message;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by jhunchar on 9/3/15.
 */
public class EventsNetworkService extends IntentService {

    private static final String TAG = EventsNetworkService.class.getName();

    public static final String NOTIFICATION = "org.septa.android.app";

    public EventsNetworkService() {
        super("EventsNetworkService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "onHandleIntent");
        }

        // Request message
        try {

            Message message = EventsRestClient.get().getMessage();

            success(message, null);
        }

        catch (RetrofitError retrofitError) {
            failure(retrofitError);
        }
    }

    public void success(Message message, Response response) {

        // Response was successful
        if (message != null) {
            handleResult(EventsConstants.VALUE_EVENTS_NETWORK_SUCCESS, message);
        }

        // Response was empty
        else {
            handleResult(EventsConstants.VALUE_EVENTS_NETWORK_ERROR, null);
        }
    }

    public void failure(RetrofitError error) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "failure");
        }

        // Network error
        handleResult(EventsConstants.VALUE_EVENTS_NETWORK_ERROR, null);
    }

    private void handleResult(String result, Object object) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "handleResult: " + result);
        }

        // Broadcast result
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(EventsConstants.KEY_RESULT, result);

        if (object != null) {
            String forecastJson = GsonObject.convertObjectToJsonString(object, false);
            intent.putExtra(EventsConstants.KEY_EVENTS_JSON_RESPONSE, forecastJson);
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
