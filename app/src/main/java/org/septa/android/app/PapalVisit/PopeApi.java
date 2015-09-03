package org.septa.android.app.PapalVisit;

import retrofit.http.GET;

/**
 * Created by jhunchar on 9/3/15.
 */
public interface PopeApi {

    @GET("/api/dbVersion/")
    Message getMessage();

    @GET("/beta/agga/dbVersion/pope.php")
    Message getDebugMessage();

}
