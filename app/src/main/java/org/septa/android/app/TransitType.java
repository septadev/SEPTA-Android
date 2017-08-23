package org.septa.android.app;

/**
 * Created by jkampf on 8/23/17.
 */

public enum TransitType {
    RAIL(R.drawable.rail_white, R.drawable.rail_final_icon_white),
    BUS(R.drawable.bus_final_icon_white, R.drawable.bus_final_icon_white),
    TROLLY(R.drawable.trolley_final_icon_white, R.drawable.trolley_final_icon_white),
    SUBWAY(R.drawable.subway_final_icon_white, R.drawable.subway_final_icon_white),
    NHSL(R.drawable.nthsl_final_icon_white, R.drawable.nthsl_final_icon_white);

    private int mapMarkerResource;
    private int tabImageResource;

    TransitType(int mapMarkerResource, int tabImageResource) {
        this.mapMarkerResource = mapMarkerResource;
        this.tabImageResource = tabImageResource;

    }

    public int getMapMarkerResource() {
        return mapMarkerResource;
    }

    public int getTabImageResource() {
        return tabImageResource;
    }


}
