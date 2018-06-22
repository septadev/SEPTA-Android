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
 */

public class AnalyticsManager {

    public static final String CUSTOM_EVENT_ABOUT = "About";
    public static final String CUSTOM_EVENT_CONNECT = "Connect with SEPTA";
    public static final String CUSTOM_EVENT_FARES_TRANSIT = "Fares and Transit Info";
    public static final String CUSTOM_EVENT_FAVORITES = "Favorites";
    public static final String CUSTOM_EVENT_NEXT_TO_ARRIVE = "Next To Arrive";
    public static final String CUSTOM_EVENT_SCHEDULE = "Schedule";
    public static final String CUSTOM_EVENT_SPECIAL_EVENTS = "Special Events";
    public static final String CUSTOM_EVENT_SYSTEM_MAP = "System Map";
    public static final String CUSTOM_EVENT_SYSTEM_STATUS = "System Status";
    public static final String CUSTOM_EVENT_TRAIN_VIEW = "TrainView";
    public static final String CUSTOM_EVENT_TRANSIT_VIEW = "TransitView";


    private static boolean initialized = false;

    public static void init(Context context) {
        Fabric.with(context, new Answers());
        initialized = true;
    }

    public static void logContentType(String tag, String contentName, String contentId, String contentType) {
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
