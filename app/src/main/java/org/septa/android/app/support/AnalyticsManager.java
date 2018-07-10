package org.septa.android.app.support;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;

import java.util.Map;

import io.fabric.sdk.android.Fabric;

/**
 * Manages Analytics
 *
 * will only log events to Crashlytics on a release build of the app
 */

public abstract class AnalyticsManager {

    private static final String TAG = AnalyticsManager.class.getSimpleName();

    // content ids
    public static final String CUSTOM_EVENT_ID_NEXT_TO_ARRIVE = "Next To Arrive";
    public static final String CUSTOM_EVENT_ID_FAVORITES = "Favorites";
    public static final String CUSTOM_EVENT_ID_SYSTEM_STATUS = "System Status";
    public static final String CUSTOM_EVENT_ID_SCHEDULE = "Schedule";
    public static final String CUSTOM_EVENT_ID_FARES_TRANSIT = "Fares and Transit Info";
    public static final String CUSTOM_EVENT_ID_SYSTEM_MAP = "System Map";
    public static final String CUSTOM_EVENT_ID_SPECIAL_EVENTS = "Special Events";
    public static final String CUSTOM_EVENT_ID_CONNECT = "Connect with SEPTA";
    public static final String CUSTOM_EVENT_ID_PERKS = "Perks";
    public static final String CUSTOM_EVENT_ID_TRANSITVIEW = "TransitView";
    public static final String CUSTOM_EVENT_ID_TRAINVIEW = "TrainView";
    public static final String CUSTOM_EVENT_ID_ABOUT = "About";

    // menu item click event names
    public static final String CUSTOM_EVENT_MENU_NEXT_TO_ARRIVE = "Next To Arrive Picker";
    public static final String CUSTOM_EVENT_MENU_FAVORITES = "Favorites";
    public static final String CUSTOM_EVENT_MENU_SYSTEM_STATUS = "System Status Picker";
    public static final String CUSTOM_EVENT_MENU_SCHEDULE = "Schedule Picker";
    public static final String CUSTOM_EVENT_MENU_FARES = "Fares and Transit Info";
    public static final String CUSTOM_EVENT_MENU_SYSTEM_MAP = "System Map";
    public static final String CUSTOM_EVENT_MENU_SPECIAL_EVENTS = "Special Events";
    public static final String CUSTOM_EVENT_MENU_CONNECT = "Connect with SEPTA";
    public static final String CUSTOM_EVENT_MENU_PERKS = "Perks";
    public static final String CUSTOM_EVENT_MENU_TRANSITVIEW = "TransitView Picker";
    public static final String CUSTOM_EVENT_MENU_TRAINVIEW = "TrainView";
    public static final String CUSTOM_EVENT_MENU_ABOUT = "About";

    // navigating from one screen to another events
    public static final String CUSTOM_EVENT_NTA_FROM_PICKER = "Next To Arrive Results (from Picker)";
    public static final String CUSTOM_EVENT_NTA_FROM_FAVORITES = "Next To Arrive Results (from Favorites)";
    public static final String CUSTOM_EVENT_NTA_FROM_SCHEDULE = "Next To Arrive Results (from Schedule Results)";

    public static final String CUSTOM_EVENT_SYSTEM_STATUS_FROM_PICKER = "System Status Results (from Picker)";
    public static final String CUSTOM_EVENT_SYSTEM_STATUS_FROM_FAVORITES = "System Status Results (from Favorites)";
    public static final String CUSTOM_EVENT_SYSTEM_STATUS_FROM_NTA = "System Status Results (from Next To Arrive Results)";
    public static final String CUSTOM_EVENT_SYSTEM_STATUS_FROM_TRANSITVIEW = "System Status Results (from TransitView Results)"; // TODO

    public static final String CUSTOM_EVENT_SCHEDULE_FROM_PICKER = "Schedule Results (from Picker)";
    public static final String CUSTOM_EVENT_SCHEDULE_FROM_FAVORITES = "Schedule Picker (from Favorites)";
    public static final String CUSTOM_EVENT_SCHEDULE_FROM_NTA = "Schedule Picker (from Next To Arrive Results)";

    public static final String CUSTOM_EVENT_TRANSITVIEW_FROM_PICKER = "TransitView Results (from Picker)";
    public static final String CUSTOM_EVENT_TRANSITVIEW_FROM_FAVORITES = "TransitView Results (from Favorites)";

    // TODO: move interactions into a different event for tracking
//    public static final String CUSTOM_EVENT_SAVE_RENAMED_FAVORITE = "Save a Renamed Favorite";
//    public static final String CUSTOM_EVENT_DELETE_FAVORITE = "Delete a Favorite";

    private static boolean initialized = false;

    public static void init(Context context) {
        Fabric.with(context, new Answers());
        initialized = true;
    }

    public static void logContentType(String tag, String contentName, String contentId, String contentType) {
        Log.d(TAG, String.format("Tag: %s Name: %s ID: %s Type: %s", tag, contentName, contentId, contentType));

        if (initialized) {
            ContentViewEvent contentViewEvent = new ContentViewEvent();

            if (contentId != null) {
                contentViewEvent.putContentId(contentId);
            }

            if (contentName != null) {
                contentViewEvent.putContentName(contentName);
            }

            if (contentType != null) {
                contentViewEvent.putContentType(contentType);
            }

            Answers.getInstance().logContentView(contentViewEvent);
        } else {
            Log.i(tag, "-----------------------------------------");
            Log.i(tag, String.format("Content Name: %s", contentName));
            Log.i(tag, String.format("Content Id: %s", contentId));
            Log.i(tag, String.format("Content Type: %s", contentType));
            Log.i(tag, "-----------------------------------------");
        }
    }

    public static void logCustomAction(String tag, String eventName, Map<String, String> customAttributes) {
        if (initialized) {
            CustomEvent customEvent = new CustomEvent(eventName);

            if (customAttributes != null && !customAttributes.isEmpty()) {
                for (String key : customAttributes.keySet()) {
                    customEvent.putCustomAttribute(key, customAttributes.get(key));
                }
            }

            Answers.getInstance().logCustom(customEvent);
        } else {
            Log.i(tag, "-----------------------------------------");
            Log.i(tag, String.format("Event: %s", eventName));

            if (customAttributes != null && !customAttributes.isEmpty()) {
                for (String attributeKey : customAttributes.keySet()) {
                    Log.i(tag, String.format("%s: %s", attributeKey, customAttributes.get(attributeKey)));
                }
            }

            Log.i(tag, "-----------------------------------------");
        }
    }
}
