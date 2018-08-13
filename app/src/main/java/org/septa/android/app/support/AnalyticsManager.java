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
    public static final String CONTENT_ID_NEXT_TO_ARRIVE = "Next To Arrive";
    public static final String CONTENT_ID_FAVORITES = "Favorites";
    public static final String CONTENT_ID_SYSTEM_STATUS = "System Status";
    public static final String CONTENT_ID_SCHEDULE = "Schedule";
    public static final String CONTENT_ID_FARES_TRANSIT = "Fares and Transit Info";
    public static final String CONTENT_ID_SYSTEM_MAP = "System Map";
    public static final String CONTENT_ID_NOTIFICATIONS = "Push Notifications";
    public static final String CONTENT_ID_CONNECT = "Connect with SEPTA";
    public static final String CONTENT_ID_PERKS = "Perks";
    public static final String CONTENT_ID_TRANSITVIEW = "TransitView";
    public static final String CONTENT_ID_TRAINVIEW = "TrainView";
    public static final String CONTENT_ID_ABOUT = "About";

    // menu item click event names
    public static final String CONTENT_VIEW_EVENT_MENU_NEXT_TO_ARRIVE = "Next To Arrive Picker";
    public static final String CONTENT_VIEW_EVENT_MENU_FAVORITES = "Favorites";
    public static final String CONTENT_VIEW_EVENT_MENU_SYSTEM_STATUS = "System Status Picker";
    public static final String CONTENT_VIEW_EVENT_MENU_SCHEDULE = "Schedule Picker";
    public static final String CONTENT_VIEW_EVENT_MENU_FARES = "Fares and Transit Info";
    public static final String CONTENT_VIEW_EVENT_MENU_SYSTEM_MAP = "System Map";
    public static final String CONTENT_VIEW_EVENT_MENU_NOTIFICATIONS = "Notification Management";
    public static final String CONTENT_VIEW_EVENT_MENU_CONNECT = "Connect with SEPTA";
    public static final String CONTENT_VIEW_EVENT_MENU_PERKS = "Perks";
    public static final String CONTENT_VIEW_EVENT_MENU_TRANSITVIEW = "TransitView Picker";
    public static final String CONTENT_VIEW_EVENT_MENU_TRAINVIEW = "TrainView";
    public static final String CONTENT_VIEW_EVENT_MENU_ABOUT = "About";

    // navigating from one screen to another events
    public static final String CONTENT_VIEW_EVENT_NTA_FROM_PICKER = "Next To Arrive Results (from Picker)";
    public static final String CONTENT_VIEW_EVENT_NTA_FROM_FAVORITES = "Next To Arrive Results (from Favorites)";
    public static final String CONTENT_VIEW_EVENT_NTA_FROM_SCHEDULE = "Next To Arrive Results (from Schedule Results)";

    public static final String CONTENT_VIEW_EVENT_SYSTEM_STATUS_FROM_PICKER = "System Status Results (from Picker)";
    public static final String CONTENT_VIEW_EVENT_SYSTEM_STATUS_FROM_FAVORITES = "System Status Results (from Favorites)";
    public static final String CONTENT_VIEW_EVENT_SYSTEM_STATUS_FROM_NTA = "System Status Results (from Next To Arrive Results)";
    public static final String CONTENT_VIEW_EVENT_SYSTEM_STATUS_FROM_TRANSITVIEW = "System Status Results (from TransitView Results)";

    public static final String CONTENT_VIEW_EVENT_SCHEDULE_FROM_PICKER = "Schedule Results (from Picker)";
    public static final String CONTENT_VIEW_EVENT_SCHEDULE_FROM_FAVORITES = "Schedule Picker (from Favorites)";
    public static final String CONTENT_VIEW_EVENT_SCHEDULE_FROM_NTA = "Schedule Picker (from Next To Arrive Results)";

    public static final String CONTENT_VIEW_EVENT_TRANSITVIEW_FROM_PICKER = "TransitView Results (from Picker)";
    public static final String CONTENT_VIEW_EVENT_TRANSITVIEW_FROM_FAVORITES = "TransitView Results (from Favorites)";

    // tapping on external links
    public static final String CUSTOM_EVENT_ID_EXTERNAL_LINK = "External Links";
    public static final String CUSTOM_EVENT_APP_FEEDBACK = "Provide App Feedback";
    public static final String CUSTOM_EVENT_SEND_COMMENT = "Send Us a Comment";
    public static final String CUSTOM_EVENT_LIVE_CHAT = "Live Chat";
    public static final String CUSTOM_EVENT_TWITTER = "Twitter";
    public static final String CUSTOM_EVENT_FACEBOOK = "Facebook";
    public static final String CUSTOM_EVENT_KEY_MORE = "More About SEPTA Key";
    public static final String CUSTOM_EVENT_FARES_MORE = "More About Fares";
    public static final String CUSTOM_EVENT_PERKS_MORE = "More About Perks";
    public static final String CUSTOM_EVENT_ELERTS = "ELERTS Transit Watch";

    // favorites actions
    public static final String CUSTOM_EVENT_ID_FAVORITES_MANAGEMENT = "Favorites Management";
    public static final String CUSTOM_EVENT_ADD_FAVORITE_BUTTON = "Tap 'Add' Favorite Button";
    public static final String CUSTOM_EVENT_EDIT_FAVORITES_BUTTON = "Tap 'Edit' Button to Switch to Edit Favorites Mode";
    public static final String CUSTOM_EVENT_CREATE_FAVORITE_NTA = "Create New NTA Favorite";
    public static final String CUSTOM_EVENT_CREATE_FAVORITE_TRANSITVIEW = "Create New TransitView Favorite";
    public static final String CUSTOM_EVENT_RENAME_FAVORITE_NTA = "Rename NTA Favorite";
    public static final String CUSTOM_EVENT_RENAME_FAVORITE_TRANSITVIEW = "Rename TransitViewFavorite";
    public static final String CUSTOM_EVENT_DELETE_FAVORITE = "Delete a Favorite";

    // push notifications actions
    public static final String CONTENT_VIEW_EVENT_NOTIFICATIONS_FROM_SYSTEM_STATUS = "Notification Management (from System Status Results)";
    public static final String CUSTOM_EVENT_ID_NOTIFICATION_MANAGEMENT = "Push Notification Management";
    public static final String CUSTOM_EVENT_ENABLE_NOTIFS = "Enable Notifications";
    public static final String CUSTOM_EVENT_DISABLE_NOTIFS = "Disable Notifications";
    public static final String CUSTOM_EVENT_ENABLE_SPECIAL_ANNOUNCEMENTS = "Enable Special Announcements";
    public static final String CUSTOM_EVENT_DISABLE_SPECIAL_ANNOUNCEMENTS = "Disable Special Announcements";

    public static final String CUSTOM_EVENT_DAYS_OF_WEEK = "Days of Week"; // TODO:
    public static final String CUSTOM_EVENT_TIMEFRAMES = "Notification Timeframe(s)"; // TODO: should this be one event to track with days of week?

    public static final String CUSTOM_EVENT_ROUTE_SUBSCRIBE = "Subscribe to Route";
    public static final String CUSTOM_EVENT_ROUTE_UNSUBSCRIBE = "Unsubscribe from Route";
    public static final String CUSTOM_EVENT_ROUTE_DELETE_SUBSCRIPTION = "Delete Route Subscription";
    public static final String CUSTOM_EVENT_NOTIF_RECEIVED = "Push Notification Received"; // TODO: notificationType, routeID
    public static final String CUSTOM_EVENT_NOTIF_SHOWN_TO_USER = "Push Notification Shown to User"; // TODO: notificationType, routeID
    public static final String CUSTOM_EVENT_NOTIF_CLICKED = "Push Notification Clicked"; // TODO: notificationType, routeID

    private static boolean initialized = false;

    public static void init(Context context) {
        Fabric.with(context, new Answers());
        initialized = true;
    }

    public static void logContentViewEvent(String tag, String contentName, String contentId, String contentType) {
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

    public static void logCustomEvent(String tag, String eventName, String eventId, Map<String, String> customAttributes) {
        Log.d(TAG, String.format("Tag: %s Name: %s ID: %s", tag, eventName, eventId));

        if (initialized) {
            CustomEvent customEvent = new CustomEvent(eventId);
            customEvent.putCustomAttribute("Event Name", eventName);

            if (customAttributes != null && !customAttributes.isEmpty()) {
                for (String key : customAttributes.keySet()) {
                    customEvent.putCustomAttribute(key, customAttributes.get(key));
                }
            }

            Answers.getInstance().logCustom(customEvent);
        } else {
            Log.i(tag, "-----------------------------------------");
            Log.i(tag, String.format("Event ID: %s", eventId));

            if (customAttributes != null && !customAttributes.isEmpty()) {
                for (String attributeKey : customAttributes.keySet()) {
                    Log.i(tag, String.format("%s: %s", attributeKey, customAttributes.get(attributeKey)));
                }
            }
            Log.i(tag, "-----------------------------------------");
        }
    }
}
