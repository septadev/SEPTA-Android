package org.septa.android.app.notifications.subscription;

import android.app.AlarmManager;
import android.content.Context;

public class AlarmManagerProvider {

    private static AlarmManager sAlarmManager;

    static synchronized AlarmManager getAlarmManager(Context context) {
        if (sAlarmManager == null) {
            sAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }

        return sAlarmManager;
    }
}
