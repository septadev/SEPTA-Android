package org.septa.android.app.models;

import java.util.HashMap;

/**
 * Created by rrobinson on 7/20/14.
 */
public enum TransportationType {
    TROLLEY, SUBWAY, RAIL, BUS;

    private static HashMap<Integer, TransportationType> transportationTypes;

    static{
        transportationTypes=new HashMap<Integer, TransportationType>();
        transportationTypes.put(0,TransportationType.TROLLEY);
        transportationTypes.put(1,TransportationType.SUBWAY);
        transportationTypes.put(2,TransportationType.RAIL);
        transportationTypes.put(3,TransportationType.BUS);
    }

    public static TransportationType getType(int typeCode){
        return transportationTypes.get(typeCode);
    }
}
