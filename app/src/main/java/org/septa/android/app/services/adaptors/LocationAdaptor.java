/*
 * LocationAdaptor.java
 * Last modified on 03-27-2014 13:30-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.services.adaptors;

import android.util.Log;

import com.google.android.gms.internal.bo;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;

import org.septa.android.app.services.ServiceErrorHandler;
import org.septa.android.app.services.apiinterfaces.LocationService;

import java.net.Proxy;
import java.util.Date;

import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

public class LocationAdaptor {
    public static final String TAG = LocationAdaptor.class.getName();

    public static LocationService getLocationService() {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(Date.class, new DateTypeAdapter())
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://www3.septa.org")       // The base API endpoint.
                .setConverter(new GsonConverter(gson))
                .setErrorHandler(new ServiceErrorHandler())
                .build();

        return restAdapter.create(LocationService.class);
    }
}
