package org.septa.android.app;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

import org.septa.android.app.support.CrashlyticsManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jkampf on 8/23/17.
 */

public enum TransitType implements Serializable {

    @SerializedName("RAIL")
    RAIL(R.drawable.pin_rail, R.drawable.ic_rail, R.drawable.rail_active_final, new RailColorProvider(), new IdLineIconProvider(R.drawable.ic_line_air), new RailAlertIdGenerator()),
    @SerializedName("BUS")
    BUS(R.drawable.pin_bus, R.drawable.ic_bus, R.drawable.bus_active_final, new BasicColorProvider(R.color.line_color_bus), new BasicLineIconProvider(R.drawable.ic_line_bus), new SimpleAlertIdGenerator("bus_route")),
    @SerializedName("TROLLEY")
    TROLLEY(R.drawable.pin_trolley, R.drawable.ic_trolley, R.drawable.trolley_active_final, new BasicColorProvider(R.color.line_color_trolley), new BasicLineIconProvider(R.drawable.ic_line_trolley), new SimpleAlertIdGenerator("trolley_route")),
    @SerializedName("SUBWAY")
    SUBWAY(R.drawable.pin_subway, R.drawable.ic_subway, R.drawable.subway_active_final, new RailColorProvider(), new IdLineIconProvider(R.drawable.ic_line_nightowl), new SimpleAlertIdGenerator("rr_route")),
    @SerializedName("NHSL")
    NHSL(R.drawable.pin_nhsl, R.drawable.ic_nhsl, R.drawable.nhsl_active_final, new BasicColorProvider(R.color.line_color_nhsl), new BasicLineIconProvider(R.drawable.ic_line_nhsl), new SimpleAlertIdGenerator("rr_route"));


    private static final String TAG = TransitType.class.getSimpleName();

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
            if (transitType.isHolidayToday())
                holidayTodayList.add(transitType);
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
            // subway and nhsl IDs must be lowercase
            List<String> lowerCaseIDs = new ArrayList<>(Arrays.asList("bso", "mfo", "bsl", "mfl", "nhsl"));
            if (lowerCaseIDs.contains(id.toLowerCase())) {
                return "rr_route_" + id.toLowerCase();
            }

            if (id.equals("LUCYGO") || id.equals("LUCYGR")) {
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
            idMap.put("air", "rr_route_apt");
            idMap.put("cyn", "rr_route_cyn");
            idMap.put("fox", "rr_route_fxc");
            idMap.put("med", "rr_route_med");
            idMap.put("nor", "rr_route_nor");
            idMap.put("pao", "rr_route_pao");
            idMap.put("tre", "rr_route_trent");
            idMap.put("wtr", "rr_route_wtren");
            idMap.put("war", "rr_route_warm");
            idMap.put("che", "rr_route_che");
            idMap.put("chw", "rr_route_chw");
            idMap.put("lan", "rr_route_landdoy");
            idMap.put("wil", "rr_route_wilm");
            idMap.put("gc", "rr_route_gc");

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
        if ("Regional Rail".equals(mode))
            return RAIL;
        if ("Trolley".equals(mode))
            return TROLLEY;
        if ("Bus".equals(mode))
            return BUS;
        if ("Broad Street Line".equals(mode))
            return SUBWAY;
        if ("Market/ Frankford".equals(mode))
            return SUBWAY;
        if ("Norristown High Speed Line".equals(mode))
            return NHSL;

        return null;
    }


}
