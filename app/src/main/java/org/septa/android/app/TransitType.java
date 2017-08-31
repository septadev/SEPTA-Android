package org.septa.android.app;

import android.content.Context;
import android.util.Log;

import java.io.Serializable;

/**
 * Created by jkampf on 8/23/17.
 */

public enum TransitType implements Serializable {

    RAIL(R.drawable.pin_rail, R.drawable.rail_final_icon_white, R.drawable.rail_active_final, new RailColorProvider(), new IdLineIconProvider(R.drawable.ic_line_air)),
    BUS(R.drawable.pin_bus, R.drawable.bus_final_icon_white, R.drawable.bus_active_final, new BasicColorProvider(R.color.line_color_bus), new BasicLineIconProvider(R.drawable.ic_line_bus)),
    TROLLY(R.drawable.pin_trolley, R.drawable.trolley_final_icon_white, R.drawable.trolley_active_final, new BasicColorProvider(R.color.line_color_trolly), new BasicLineIconProvider(R.drawable.ic_line_trolly)),
    SUBWAY(R.drawable.pin_subway, R.drawable.subway_final_icon_white, R.drawable.subway_active_final, new RailColorProvider(), new IdLineIconProvider(R.drawable.ic_line_bus)),
    NHSL(R.drawable.pin_nhsl, R.drawable.nthsl_final_icon_white, R.drawable.nhsl_active_final, new BasicColorProvider(R.color.line_color_nhsl), new BasicLineIconProvider(R.drawable.ic_line_nhsl));


    private static final String TAG = "org.septa.android.app.TransitType";

    private int mapMarkerResource;
    private int tabInactiveImageResource;
    private int tabActiveImageResource;
    private ColorProvider colorProvider;
    private LineIconProvider lineIconProvider;

    TransitType(int mapMarkerResource, int tabInactiveImageResource, int tabActiveImageResource, ColorProvider colorProvider, LineIconProvider lineIconProvider) {
        this.mapMarkerResource = mapMarkerResource;
        this.tabInactiveImageResource = tabInactiveImageResource;
        this.tabActiveImageResource = tabActiveImageResource;
        this.colorProvider = colorProvider;
        this.lineIconProvider = lineIconProvider;
    }

    public int getMapMarkerResource() {
        return mapMarkerResource;
    }

    public int getTabInactiveImageResource() {
        return tabInactiveImageResource;
    }

    public int getTabActiveImageResource() {
        return tabActiveImageResource;
    }

    public int getLineColor(String lineId, Context context) {
        return colorProvider.getColorForLine(lineId, context);
    }

    public int getIconForLine(String lineId, Context context) {
        return lineIconProvider.getIconForLine(lineId, context);
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

    private interface LineIconProvider {
        int getIconForLine(String lineId, Context context);
    }

    protected static class BasicLineIconProvider implements LineIconProvider {
        private final int dafaultValue;

        BasicLineIconProvider(int defaultValue) {
            this.dafaultValue = defaultValue;
        }

        @Override
        public int getIconForLine(String lineId, Context context) {
            return dafaultValue;
        }
    }

    protected static class IdLineIconProvider implements LineIconProvider {

        private final int defaultValue;

        IdLineIconProvider(int defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public int getIconForLine(String lineId, Context context) {
            try {
                int returnVal =  context.getResources().getIdentifier("ic_line_" + lineId.toLowerCase(), "drawable", R.class.getPackage().getName());
                Log.d(TAG, "IconForLine: " + returnVal + " for " + lineId.toLowerCase());
                if (returnVal == 0)
                    return defaultValue;
                return  returnVal;
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "IconForLine: returning default value:" + defaultValue);
                return defaultValue;
            }
        }
    }
}
