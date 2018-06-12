package org.septa.android.app.locationpicker;

import android.content.Context;

public class LocationPickerUtils {

    // location picker shared preferences
    private static final String SHARED_PREFERENCES_LOCATION_PICKER = "SHARED_PREFERENCES_LOCATION_PICKER";
    private static final String STOP_PICKER_SORT_ORDER = "STOP_PICKER_SORT_ORDER";

    /**
     * returns user's last used sort order preference
     * 0 : alphabetical
     * 1 : reverse alphabetical
     * 2 : in order
     * @param context
     * @return sort order
     */
    public static int getStopPickerSortOrder(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_LOCATION_PICKER, Context.MODE_PRIVATE).getInt(STOP_PICKER_SORT_ORDER, 0);
    }

    /**
     * saves user's most recently used sort order preference
     * 0 : alphabetical
     * 1 : reverse alphabetical
     * 2 : in order
     * @param context
     * @param sortOrder
     */
    public static void setStopPickerSortOrder(Context context, int sortOrder) {
        context.getSharedPreferences(SHARED_PREFERENCES_LOCATION_PICKER, Context.MODE_PRIVATE).edit().putInt(STOP_PICKER_SORT_ORDER, sortOrder).apply();
    }
}
