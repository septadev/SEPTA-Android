package org.septa.android.app.notifications.edit;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.septa.android.app.R;
import org.septa.android.app.notifications.NotificationItemAdapter;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.RouteNotificationSubscription;

import java.util.List;

public class EditNotificationsFragment extends android.support.v4.app.Fragment {

    private EditNotificationsFragmentListener mListener;
    private List<RouteNotificationSubscription> routesList;

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
            notificationItemAdapter = new NotificationItemAdapter(activity, routesList, true);
            notificationRecyclerView.setAdapter(notificationItemAdapter);
            notificationItemAdapter.updateList(routesList);
        }

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(context instanceof EditNotificationsFragmentListener)) {
            throw new RuntimeException("Context must implement EditNotificationsFragmentListener");
        } else {
            mListener = (EditNotificationsFragmentListener) context;
        }
    }

    public void deleteNotificationAtPosition(int position) {
        routesList.remove(position);
        notificationItemAdapter.notifyItemRemoved(position);
        notificationItemAdapter.notifyDataSetChanged();

        // close edit mode if no favorites left
        if (routesList.isEmpty()) {
            mListener.closeEditMode();
        }
    }

    public interface EditNotificationsFragmentListener {
        void closeEditMode();
    }

}