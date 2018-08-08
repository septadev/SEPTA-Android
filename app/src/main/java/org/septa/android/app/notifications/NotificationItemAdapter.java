package org.septa.android.app.notifications;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.notifications.edit.NotificationDiffCallback;
import org.septa.android.app.services.apiinterfaces.model.RouteNotificationSubscription;
import org.septa.android.app.support.CrashlyticsManager;

import java.util.List;

public class NotificationItemAdapter extends RecyclerView.Adapter<NotificationItemAdapter.NotificationViewHolder> {

    private static final String TAG = NotificationItemAdapter.class.getSimpleName();

    private Activity activity;
    private NotificationItemListener mListener;
    private List<RouteNotificationSubscription> mRoutesList;
    private boolean isInEditMode;

    public NotificationItemAdapter(Activity activity, List<RouteNotificationSubscription> routesList, boolean isInEditMode) {
        this.activity = activity;
        if (activity instanceof NotificationItemListener) {
            this.mListener = (NotificationItemListener) activity;
        } else {
            IllegalArgumentException iae = new IllegalArgumentException("Context Must Implement NotificationItemListener");
            CrashlyticsManager.log(Log.ERROR, TAG, iae.toString());
            throw iae;
        }
        this.mRoutesList = routesList;
        this.isInEditMode = isInEditMode;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NotificationViewHolder holder, int position) {
        final RouteNotificationSubscription route = mRoutesList.get(position);
        final String routeId = route.getRouteId();
        final String routeName = route.getRouteName();
        final TransitType transitType = route.getTransitType();
        boolean isEnabled = route.isEnabled();

        // set route ID name
        holder.routeNameText.setText(routeName);

        // create color bullet
        int color = ContextCompat.getColor(activity, transitType.getLineColor(routeId, activity));
        Drawable bullet = ContextCompat.getDrawable(activity, R.drawable.shape_line_marker);
        bullet.setColorFilter(color, PorterDuff.Mode.SRC);

        // place transit type icon on left and color bullet on right
        holder.routeNameText.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(activity, transitType.getTabActiveImageResource()), null, bullet, null);

        if (isInEditMode) {
            // hide switch
            holder.notifSwitch.setVisibility(View.GONE);

            // delete button
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.promptToDeleteNotification(holder.getAdapterPosition(), routeId, routeName, transitType);
                }
            });
        } else {
            // hide delete button
            holder.deleteButton.setVisibility(View.GONE);

            // initial switch position
            holder.notifSwitch.setChecked(isEnabled);

            // toggle notifications for this route
            holder.notifSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        // enable notifs for route
                        PushNotificationManager.getInstance(activity).createNotificationForRoute(routeId, routeName, transitType);
                    } else {
                        // disable notifs for route
                        PushNotificationManager.getInstance(activity).removeNotificationForRoute(routeId, transitType);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mRoutesList.size();
    }

    public void updateList(List<RouteNotificationSubscription> routesList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new NotificationDiffCallback(this.mRoutesList, routesList));
        this.mRoutesList = routesList;

        // make changes to view
        diffResult.dispatchUpdatesTo(this);
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        LinearLayout notificationContainer;
        TextView routeNameText;
        SwitchCompat notifSwitch;
        ImageButton deleteButton;

        NotificationViewHolder(final View view) {
            super(view);
            notificationContainer = view.findViewById(R.id.notification_item_row);
            routeNameText = view.findViewById(R.id.notification_route_id);
            notifSwitch = view.findViewById(R.id.notification_route_switch);
            deleteButton = view.findViewById(R.id.notification_delete_button);
        }
    }

    public interface NotificationItemListener {
        void promptToDeleteNotification(int position, String routeId, String routeName, TransitType transitType);
    }
}
