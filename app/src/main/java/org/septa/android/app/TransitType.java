package org.septa.android.app;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

import org.septa.android.app.support.CrashlyticsManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum TransitType implements Serializable {

    @SerializedName("RAIL")
    RAIL(R.drawable.pin_rail, R.drawable.ic_rail, R.drawable.ic_rail_circle, new RailColorProvider(), new IdLineIconProvider(R.drawable.ic_line_air), new RailAlertIdGenerator()),
    @SerializedName("BUS")
    BUS(R.drawable.pin_bus, R.drawable.ic_bus, R.drawable.ic_bus_circle, new BasicColorProvider(R.color.line_color_bus), new BasicLineIconProvider(R.drawable.ic_line_bus), new SimpleAlertIdGenerator("bus_route")),
    @SerializedName("TROLLEY")
    TROLLEY(R.drawable.pin_trolley, R.drawable.ic_trolley, R.drawable.ic_trolley_circle, new BasicColorProvider(R.color.line_color_trolley), new BasicLineIconProvider(R.drawable.ic_line_trolley), new SimpleAlertIdGenerator("trolley_route")),
    @SerializedName("SUBWAY")
    SUBWAY(R.drawable.pin_subway, R.drawable.ic_subway, R.drawable.ic_subway_circle, new RailColorProvider(), new IdLineIconProvider(R.drawable.ic_line_nightowl), new SimpleAlertIdGenerator("rr_route")),
    @SerializedName("NHSL")
    NHSL(R.drawable.pin_nhsl, R.drawable.ic_nhsl, R.drawable.ic_nhsl_circle, new BasicColorProvider(R.color.line_color_nhsl), new BasicLineIconProvider(R.drawable.ic_line_nhsl), new SimpleAlertIdGenerator("rr_route"));

    private static final String TAG = TransitType.class.getSimpleName();
    private static final String PREFIX_RR_ROUTE = "rr_route";

    private int mapMarkerResource;
    private int tabInactiveImageResource;
    private int tabActiveImageResource;
    private ColorProvider colorProvider;
    private LineIconProvider lineIconProvider;
    private AlertIdGenerator alertIdGenerator;

    private boolean holidayToday = false;

    TransitType(int mapMarkerResource, int tabInactiveImageResource, int tabActiveImageResource, ColorProvider colorProvider, LineIconProvider lineIconProvider, AlertIdGenerator alertIdGenerator) {
        this.mapMarkerResource = mapMarkerResource;
        this.tabInactiveImageResource = tabInactiveImageResource;
        this.tabActiveImageResource = tabActiveImageResource;
        this.colorProvider = colorProvider;
        this.lineIconProvider = lineIconProvider;
        this.alertIdGenerator = alertIdGenerator;
    }

    public boolean isHolidayToday() {
        return holidayToday;
    }

    public void setHolidayToday(boolean holiday) {
        holidayToday = holiday;
    }

    public static List<TransitType> transitTypesOnHolidayToday() {
        List<TransitType> holidayTodayList = new ArrayList<TransitType>();
        for (TransitType transitType : values()) {
            if (transitType.isHolidayToday()) {
                holidayTodayList.add(transitType);
            }
        }

        return holidayTodayList;
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
        if (context == null) {
            CrashlyticsManager.log(Log.ERROR, TAG, "getString method, context is null. Request was:" + name);
            return "";
        }

        int resId = context.getResources().getIdentifier(name + "_" + name().toLowerCase(), "string", R.class.getPackage().getName());
        if (resId == 0) {
            CrashlyticsManager.log(Log.ERROR, TAG, "Unable to find value for string.  Request was:" + name + "_" + name().toLowerCase());
            CrashlyticsManager.logException(TAG, new Exception("Unable to find value for string.  Request was:" + name + "_" + name().toLowerCase()));
            return "";
        }

        return context.getString(resId);
    }


    public String getAlertId(String id) {
        return alertIdGenerator.getAlertId(id);
    }

    public String getLineIdFromAlertId(String id) {
        return alertIdGenerator.getLineId(id);
    }

    private interface ColorProvider {
        int getColorForLine(String lineId, Context context);
    }

    protected static class BasicColorProvider implements ColorProvider {
        private int defaultColor;

        BasicColorProvider(int defaultColor) {
            this.defaultColor = defaultColor;
        }

        @Override
        public int getColorForLine(String lineId, Context context) {
            return defaultColor;
        }
    }

    protected static class RailColorProvider implements ColorProvider {

        @Override
        public int getColorForLine(String lineId, Context context) {
            try {
                return context.getResources().getIdentifier("line_color_" + lineId.toLowerCase(), "color", R.class.getPackage().getName());
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                return R.color.default_line_color;
            }
        }
    }

    private interface LineIconProvider {
        int getIconForLine(String lineId, Context context);
    }

    protected static class BasicLineIconProvider implements LineIconProvider {
        private final int defaultValue;

        BasicLineIconProvider(int defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public int getIconForLine(String lineId, Context context) {
            return defaultValue;
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
                if (returnVal == 0) {
                    return defaultValue;
                }
                return returnVal;
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                Log.d(TAG, "IconForLine: returning default value:" + defaultValue);
                return defaultValue;
            }
        }
    }

    private interface AlertIdGenerator {
        String getAlertId(String id);

        String getLineId(String alertId);
    }

    protected static class SimpleAlertIdGenerator implements AlertIdGenerator {
        String base;

        protected SimpleAlertIdGenerator(String base) {
            this.base = base;
        }

        @Override
        public String getAlertId(String id) {
            // the NHSL line can be picked from trolley
            // BSO and MFO can be picked from buses
            // all 3 listed above must use rr_route prefix to hit API
            if (id.equalsIgnoreCase("NHSL") || id.equalsIgnoreCase("BSO") || id.equalsIgnoreCase("MFO")) {
                return PREFIX_RR_ROUTE + "_" + id;
            }

            // Gold and Green loops through U-City need to use LUCY route ID for alerts
            if (id.equalsIgnoreCase("LUCYGO") || id.equalsIgnoreCase("LUCYGR")) {
                id = "LUCY";
            }

            return base + "_" + id;
        }

        @Override
        public String getLineId(String alertId) {
            return alertId.substring(alertId.lastIndexOf("_") + 1);
        }
    }

    protected static class RailAlertIdGenerator implements AlertIdGenerator {

        Map<String, String> idMap = new HashMap<String, String>();
        Map<String, String> revIdMap = new HashMap<String, String>();

        protected RailAlertIdGenerator() {
            idMap.put("air", PREFIX_RR_ROUTE + "_apt");
            idMap.put("cyn", PREFIX_RR_ROUTE + "_cyn");
            idMap.put("fox", PREFIX_RR_ROUTE + "_fxc");
            idMap.put("med", PREFIX_RR_ROUTE + "_med");
            idMap.put("nor", PREFIX_RR_ROUTE + "_nor");
            idMap.put("pao", PREFIX_RR_ROUTE + "_pao");
            idMap.put("tre", PREFIX_RR_ROUTE + "_trent");
            idMap.put("wtr", PREFIX_RR_ROUTE + "_wtren");
            idMap.put("war", PREFIX_RR_ROUTE + "_warm");
            idMap.put("che", PREFIX_RR_ROUTE + "_che");
            idMap.put("chw", PREFIX_RR_ROUTE + "_chw");
            idMap.put("lan", PREFIX_RR_ROUTE + "_landdoy");
            idMap.put("wil", PREFIX_RR_ROUTE + "_wilm");
            idMap.put("gc", PREFIX_RR_ROUTE + "_gc");

            for (Map.Entry<String, String> entry : idMap.entrySet()) {
                revIdMap.put(entry.getValue(), entry.getKey());
            }
        }

        @Override
        public String getAlertId(String id) {
            return idMap.get(id.toLowerCase());
        }

        @Override
        public String getLineId(String alertId) {
            return revIdMap.get(alertId.toLowerCase());
        }
    }

    public TransitType getTransitTypeByAlertMode(String mode) {
        if ("Regional Rail".equals(mode)) {
            return RAIL;
        }
        if ("Trolley".equals(mode)) {
            return TROLLEY;
        }
        if ("Bus".equals(mode)) {
            return BUS;
        }
        if ("Broad Street Line".equals(mode)) {
            return SUBWAY;
        }
        if ("Market/ Frankford".equals(mode)) {
            return SUBWAY;
        }
        if ("Norristown High Speed Line".equals(mode)) {
            return NHSL;
        }

        return null;
    }

}
