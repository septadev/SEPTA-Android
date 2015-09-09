package org.septa.android.app.events.network;

import org.septa.android.app.events.model.Message;

import retrofit.http.GET;

/**
 * Created by jhunchar on 9/3/15.
 */
public interface EventsApi {

    @GET("/api/dbVersion/")
    Message getMessage();

    @GET("/beta/agga/dbVersion/pope.php")
    Message getDebugMessage();

}
