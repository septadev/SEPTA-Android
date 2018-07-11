package org.septa.android.app.notifications;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.septa.android.app.R;
import org.septa.android.app.view.TextView;

public class NotificationsManagementFragment extends Fragment {

    private static final String TAG = NotificationsManagementFragment.class.getSimpleName(), TOOLBAR_TITLE = "TOOLBAR_TITLE";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_notifications, container, false);

        final Context context = getContext();
        if (context == null) {
            return rootView;
        }

        TextView systemSettings = rootView.findViewById(R.id.system_notification_settings);
        systemSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSystemNotificationSettings(context);
            }
        });

        TextView myNotifs = rootView.findViewById(R.id.my_notifications);
        myNotifs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: open MyNotificationsActivity
                Toast.makeText(context, "My Notifications", Toast.LENGTH_SHORT).show();
            }
        });

        TextView notifsSchedule = rootView.findViewById(R.id.notifications_schedule);
        notifsSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: open NotificationsScheduleActivity
                Toast.makeText(context, "Notification Schedule", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TOOLBAR_TITLE, getActivity().getTitle().toString());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            String title = savedInstanceState.getString(TOOLBAR_TITLE);
            if (title != null && getActivity() != null) {
                getActivity().setTitle(title);
            }
        }
    }

    private void openSystemNotificationSettings(Context context) {
        Intent intent = new Intent();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // open notification settings for SEPTA app if on 26+
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());

        } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // notification settings for 21+
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", context.getPackageName());
            intent.putExtra("app_uid", context.getApplicationInfo().uid);

        } else {
            // notification settings for <20
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
        }

        context.startActivity(intent);
    }

}
