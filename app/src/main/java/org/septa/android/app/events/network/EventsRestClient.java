package org.septa.android.app.events.network;

import com.squareup.okhttp.OkHttpClient;

import retrofit.RestAdapter;
import retrofit.RestAdapter.Builder;
import retrofit.RestAdapter.LogLevel;
import retrofit.client.OkClient;

/**
 * Created by jhunchar on 9/3/15.
 */
public class EventsRestClient {

    private static EventsApi REST_CLIENT;
    private static String ROOT = "http://www3.septa.org/";

    static {
        setupEventsRestClient();
    }

    private EventsRestClient() {
    }

    public static EventsApi get() {
        return REST_CLIENT;
    }

    private static void setupEventsRestClient() {
        Builder builder = new Builder()
                .setEndpoint(ROOT)
                .setLogLevel(LogLevel.FULL)
                .setClient(new OkClient(new OkHttpClient()));

        RestAdapter restAdapter = builder.build();
        REST_CLIENT = restAdapter.create(EventsApi.class);
    }
}
