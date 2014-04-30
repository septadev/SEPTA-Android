/*
 * ObjectFactory.java
 * Last modified on 04-29-2014 13:10-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;

public class ObjectFactory {
    private static final String TAG = ObjectFactory.class.getName();

    private static ObjectFactory instance= null;
    private static Object mutex= new Object();

    private static RoutesModel busRoutesSignleton = null;
    private static Object busRoutesMutex = new Object();
    private static RoutesModel railRoutesSignleton = null;
    private static Object railRoutesMutex = new Object();
    private static RoutesModel trolleyRoutesSignleton = null;
    private static Object trolleyRoutesMutex = new Object();


    private static HashMap<String, KMLModel> kmlModels = new HashMap<String, KMLModel>();
    private static Object kmlModelMutex = new Object();

    private ObjectFactory(){
    }

    public static ObjectFactory getInstance(){
        if(instance==null){
            synchronized (mutex){
                if(instance==null) instance= new ObjectFactory();
            }
        }

        return instance;
    }

    public RoutesModel getBusRoutes() {
        if (busRoutesSignleton == null) {
            synchronized (busRoutesMutex) {
                if (busRoutesSignleton == null) {
                    busRoutesSignleton = new RoutesModel(RoutesModel.RouteType.BUS_ROUTE);
                }
            }
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

    public RoutesModel getTrolleyRoutes() {
        if (trolleyRoutesSignleton == null) {
            synchronized (trolleyRoutesMutex) {
                if (trolleyRoutesSignleton == null) {
                    trolleyRoutesSignleton = new RoutesModel(RoutesModel.RouteType.TROLLEY_ROUTE);
                }
            }
        }

        return trolleyRoutesSignleton;
    }

    public KMLModel getKMLModel(Context context, String kmlFileName) {
        KMLModel model = kmlModels.get(kmlFileName);

        if (model == null) {
            synchronized (kmlModelMutex) {
                if (model == null) {
                    model = KMLModel.processKMLFile(context, kmlFileName);
                }
            }
        }

        kmlModels.put(kmlFileName, model);
        return model;
    }
}
