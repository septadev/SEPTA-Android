package org.septa.android.app.notifications;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.parceler.Parcels;
import org.septa.android.app.R;
import org.septa.android.app.services.apiinterfaces.model.RouteSubscription;
import org.septa.android.app.support.CrashlyticsManager;

import java.util.ArrayList;
import java.util.List;

public class ViewNotificationsFragment extends android.support.v4.app.Fragment {

    private static final String TAG = ViewNotificationsFragment.class.getSimpleName();

    private List<RouteSubscription> mRoutesList;

    private static final String KEY_ROUTE_SUBSCRIPTIONS = "KEY_ROUTE_SUBSCRIPTIONS";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        restoreArgs();

        View rootView = inflater.inflate(R.layout.fragment_view_notifications, container, false);

        Activity activity = getActivity();

        if (activity != null) {
            RecyclerView notificationRecyclerView = rootView.findViewById(R.id.notification_recyclerview);
            notificationRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
            NotificationItemAdapter notificationItemAdapter = new NotificationItemAdapter(activity, mRoutesList, false);
            notificationRecyclerView.setAdapter(notificationItemAdapter);
            notificationItemAdapter.updateList(mRoutesList);
        }

        return rootView;
    }

    public static ViewNotificationsFragment newInstance(List<RouteSubscription> routesList) {
        ViewNotificationsFragment fragment = new ViewNotificationsFragment();

        ArrayList<Parcelable> parcelRoutes = new ArrayList<>();
        if (routesList != null) {
            for (RouteSubscription route : routesList) {
                parcelRoutes.add(Parcels.wrap(route));
            }
        }
        Bundle args = new Bundle();
        args.putParcelableArrayList(KEY_ROUTE_SUBSCRIPTIONS, parcelRoutes);
        fragment.setArguments(args);

        return fragment;
    }

    private void restoreArgs() {
        Bundle bundle = getArguments();
        if (bundle == null) {
            CrashlyticsManager.log(Log.ERROR, TAG, "Null args");
        } else {
            mRoutesList = new ArrayList<>();
            ArrayList<Parcelable> parcelRoutes = bundle.getParcelableArrayList(KEY_ROUTE_SUBSCRIPTIONS);
            if (parcelRoutes != null) {
                for (Parcelable route : parcelRoutes) {
                    mRoutesList.add((RouteSubscription) Parcels.unwrap(route));
                }
            } else {
                CrashlyticsManager.log(Log.ERROR, TAG, "List of route subscriptions is null");
            }
        }
    }

    public List<RouteSubscription> getRoutesList() {
        return mRoutesList;
    }
}