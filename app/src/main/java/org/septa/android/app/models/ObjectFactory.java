/*
 * ObjectFactory.java
 * Last modified on 04-29-2014 13:10-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models;

import android.util.Log;

public class ObjectFactory {
    private static final String TAG = ObjectFactory.class.getName();

    private static ObjectFactory instance= null;
    private static Object mutex= new Object();

    private static RoutesModel busRoutesSignleton = null;
    private static Object busRoutesMutex = new Object();
    private static RoutesModel railRoutesSignleton = null;
    private static Object railRoutesMutex = new Object();

    private ObjectFactory(){
    }

    public static ObjectFactory getInstance(){
        Log.v(TAG, "getInstance in the ObjectFactory");
        if(instance==null){
            Log.d(TAG, "the ObjectFactory is null, instanciate");
            synchronized (mutex){
                if(instance==null) instance= new ObjectFactory();
            }
        } else {
            Log.d(TAG, "the ObjectFactor is not null, return the instance");
        }

        return instance;
    }

    public RoutesModel getBusRoutes() {
        Log.d(TAG, "getBusRoutes in the ObjectFactory");
        if (busRoutesSignleton == null) {
            Log.d(TAG, "busRoutesSingleton is null, instanciate");
            synchronized (busRoutesMutex) {
                if (busRoutesSignleton == null) {
                    busRoutesSignleton = new RoutesModel(RoutesModel.RouteType.BUS_ROUTE);
                }
            }
        } else {
            Log.d(TAG, "the busRoutesSingleton is not null, return the instance");
        }

        return busRoutesSignleton;
    }

    public RoutesModel getRailRoutes() {
        if (railRoutesSignleton == null) {
            synchronized (railRoutesMutex) {
                if (railRoutesSignleton == null) {
                    railRoutesSignleton = new RoutesModel(RoutesModel.RouteType.RAIL_ROUTE);
                }
            }
        }

        return railRoutesSignleton;
    }
}
