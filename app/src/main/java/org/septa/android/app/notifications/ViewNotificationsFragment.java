package org.septa.android.app.notifications;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.septa.android.app.R;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.RouteSubscription;

import java.util.List;

public class ViewNotificationsFragment extends android.support.v4.app.Fragment {

    private List<RouteSubscription> routesList;

    private RecyclerView notificationRecyclerView;
    private NotificationItemAdapter notificationItemAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_view_notifications, container, false);

        Activity activity = getActivity();

        if (activity != null) {
            routesList = SeptaServiceFactory.getNotificationsService().getRoutesSubscribedTo(activity);
            notificationRecyclerView = rootView.findViewById(R.id.notification_recyclerview);
            notificationRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
            notificationItemAdapter = new NotificationItemAdapter(activity, routesList, false);
            notificationRecyclerView.setAdapter(notificationItemAdapter);
            notificationItemAdapter.updateList(routesList);
        }

        return rootView;
    }

}