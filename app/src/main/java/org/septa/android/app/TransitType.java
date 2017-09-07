package org.septa.android.app;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by jkampf on 8/23/17.
 */

public enum TransitType implements Serializable {

    @SerializedName("RAIL")
    RAIL(R.drawable.pin_rail, R.drawable.ic_rail, R.drawable.rail_active_final, new RailColorProvider(), new IdLineIconProvider(R.drawable.ic_line_air)),
    @SerializedName("BUS")
    BUS(R.drawable.pin_bus, R.drawable.ic_bus, R.drawable.bus_active_final, new BasicColorProvider(R.color.line_color_bus), new BasicLineIconProvider(R.drawable.ic_line_bus)),
    @SerializedName("TROLLEY")
    TROLLEY(R.drawable.pin_trolley, R.drawable.ic_trolley, R.drawable.trolley_active_final, new BasicColorProvider(R.color.line_color_trolley), new BasicLineIconProvider(R.drawable.ic_line_trolley)),
    @SerializedName("SUBWAY")
    SUBWAY(R.drawable.pin_subway, R.drawable.ic_subway, R.drawable.subway_active_final, new RailColorProvider(), new IdLineIconProvider(R.drawable.ic_line_bus)),
    @SerializedName("NHSL")
    NHSL(R.drawable.pin_nhsl, R.drawable.ic_nhsl, R.drawable.nhsl_active_final, new BasicColorProvider(R.color.line_color_nhsl), new BasicLineIconProvider(R.drawable.ic_line_nhsl));


    private static final String TAG = "org.septa...TransitType";

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

    public String getString(String name, Context context) {
        int resId = context.getResources().getIdentifier(name + "_" + name().toLowerCase(), "string", R.class.getPackage().getName());
        return context.getString(resId);
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
                int returnVal = context.getResources().getIdentifier("ic_line_" + lineId.toLowerCase(), "drawable", R.class.getPackage().getName());
                Log.d(TAG, "IconForLine: " + returnVal + " for " + lineId.toLowerCase());
                if (returnVal == 0)
                    return defaultValue;
                return returnVal;
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "IconForLine: returning default value:" + defaultValue);
                return defaultValue;
            }
        }
    }


}
