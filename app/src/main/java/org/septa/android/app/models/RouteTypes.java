/*
 * RouteTypes.java
 * Last modified on 05-07-2014 11:12-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models;
@Deprecated
public enum RouteTypes {
    TROLLEY ("trolley"),
    MFL ("mfl"),
    RAIL ("rail"),
    BUS ("bus"),
    BSL ("bsl"),
    NHSL ("nhsl"),
    TRACKLESSTROLLEY ("tracklesstrolley");

    private final String name;

    private RouteTypes(String s) {
        name = s;
    }

    public boolean equalsName(String otherName){
        return (otherName == null)? false:name.equals(otherName);
    }

    public String toString(){
        return name;
    }

}
