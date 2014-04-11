/*
 * ServiceErrorHandler.java
 * Last modified on 04-11-2014 08:32-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.services;

import android.util.Log;

import org.septa.android.app.services.adaptors.LocationAdaptor;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ServiceErrorHandler implements ErrorHandler {
    public static final String TAG = LocationAdaptor.class.getName();

    @Override
    public Throwable handleError(RetrofitError cause) {

        Log.d(TAG, "cause? " + cause.getMessage());

        Log.d(TAG, "url? " + cause.getUrl());

        Response r = cause.getResponse();
        if (r != null) {

            return new Exception("a service call exception");
        }
        return cause;
    }
}