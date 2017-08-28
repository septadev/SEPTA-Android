package org.septa.android.app;

import android.content.Context;

import java.io.Serializable;

/**
 * Created by jkampf on 8/23/17.
 */

public enum TransitType implements Serializable {
    RAIL(R.drawable.pin_rail, R.drawable.rail_final_icon_white, new RailColorProvider()),
    BUS(R.drawable.pin_bus, R.drawable.bus_final_icon_white, new BasicColorProvider(R.color.line_color_bus)),
    TROLLY(R.drawable.pin_trolley, R.drawable.trolley_final_icon_white, new BasicColorProvider(R.color.line_color_trolly)),
    SUBWAY(R.drawable.pin_subway, R.drawable.subway_final_icon_white, new RailColorProvider()),
    NHSL(R.drawable.pin_nhsl, R.drawable.nthsl_final_icon_white, new BasicColorProvider(R.color.line_color_nhsl));

    private int mapMarkerResource;
    private int tabImageResource;
    private ColorProvider colorProvider;

    TransitType(int mapMarkerResource, int tabImageResource, ColorProvider colorProvider) {
        this.mapMarkerResource = mapMarkerResource;
        this.tabImageResource = tabImageResource;
        this.colorProvider = colorProvider;

    }

    public int getMapMarkerResource() {
        return mapMarkerResource;
    }

    public int getTabImageResource() {
        return tabImageResource;
    }

    public int getLineColor(String lineId, Context context) {
        return colorProvider.getColorForLine(lineId, context);
    }

    private interface ColorProvider {
        int getColorForLine(String lineId, Context context);
    }

    protected static class BasicColorProvider implements ColorProvider {
        private int defaultCololor;

        BasicColorProvider(int defaultColor) {
            this.defaultCololor = defaultColor;
        }

        @Override
        public int getColorForLine(String lineId, Context context) {
            return defaultCololor;
        }
    }

    protected static class RailColorProvider implements ColorProvider {

        @Override
        public int getColorForLine(String lineId, Context context) {
            try {
                return context.getResources().getIdentifier("line_color_" + lineId.toLowerCase(), "color", R.class.getPackage().getName());
            } catch (Exception e) {
                e.printStackTrace();
                return R.color.default_line_color;
            }
        }
    }
}
