package org.septa.android.app.notifications.subscription;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;

import java.util.Calendar;

public class AutoSubscriptionReceiver extends BroadcastReceiver {

    private static final String TAG = AutoSubscriptionReceiver.class.getSimpleName();

    private static final int NUM_DAYS_BEFORE_AUTOSUBSCRIBING = 1; // TODO: change to 5

    @Override
    public void onReceive(Context context, Intent intent) {
        // schedule alarm on BOOT_COMPLETED
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            scheduleSubscriptionUpdate(context, true);
        }
    }

    public static void scheduleSubscriptionUpdate(Context context, boolean onBoot) {
        AlarmManager manager = AlarmManagerProvider.getAlarmManager(context);

        boolean enabled = SeptaServiceFactory.getNotificationsService().areNotificationsEnabled(context);

        // intent to trigger
        Intent intent = new Intent(context, AutoSubscriptionService.class);
        PendingIntent operation = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (enabled) {
            // gather the time preference
            Calendar startTime = Calendar.getInstance();

            if (onBoot) {
                // resume previous alarm set this is so we do not restart the clock every time the device is rebooted
                startTime.setTimeInMillis(SeptaServiceFactory.getNotificationsService().getNextAutoSubscriptionTime(context));

            } else {
                // cancel existing alarm
                manager.cancel(operation);

                // set new alarm time
                try {
                    startTime.setTimeInMillis(System.currentTimeMillis());
                    startTime.add(Calendar.HOUR_OF_DAY, 1); // TODO: switch back
//                    startTime.add(Calendar.DAY_OF_MONTH, NUM_DAYS_BEFORE_AUTOSUBSCRIBING); // TODO: put back
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Unable to determine alarm start time", e);
                    return;
                }
            }

            SeptaServiceFactory.getNotificationsService().setNextAutoSubscriptionTime(context, startTime.getTimeInMillis());

            Log.d(TAG, "Scheduling auto-subscription alarm: " + startTime.getTime().toString() + " onBoot: " + onBoot);
//            manager.setRepeating(AlarmManager.RTC_WAKEUP, startTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY * NUM_DAYS_BEFORE_AUTOSUBSCRIBING, operation); // TODO: put back
            manager.setRepeating(AlarmManager.RTC_WAKEUP, startTime.getTimeInMillis(), AlarmManager.INTERVAL_HOUR * NUM_DAYS_BEFORE_AUTOSUBSCRIBING, operation); // TODO: remove
        } else {
            cancelSubscriptionUpdate(context);
        }
    }

    public static void cancelSubscriptionUpdate(Context context) {
        AlarmManager manager = AlarmManagerProvider.getAlarmManager(context);
        Intent intent = new Intent(context, AutoSubscriptionService.class);
        PendingIntent operation = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Log.d(TAG, "Disabling auto-subscription alarm");
        manager.cancel(operation);
    }

}