package org.septa.android.app.notifications;

import android.content.Context;
import android.os.Bundle;
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
                // TODO: make do something
                Toast.makeText(context, "System Settings", Toast.LENGTH_SHORT).show();
            }
        });

        TextView myNotifs = rootView.findViewById(R.id.my_notifications);
        myNotifs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: make do something
                Toast.makeText(context, "My Notifications", Toast.LENGTH_SHORT).show();
            }
        });

        TextView notifsSchedule = rootView.findViewById(R.id.notifications_schedule);
        notifsSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: make do something
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
}
