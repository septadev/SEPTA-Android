package org.septa.android.app.notifications;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import org.septa.android.app.R;
import org.septa.android.app.notifications.timepicker.NotificationTimePickerDialogListener;
import org.septa.android.app.notifications.timepicker.TimePickerDialog;
import org.septa.android.app.services.apiinterfaces.NotificationsSharedPrefsUtilsImpl;
import org.septa.android.app.support.CrashlyticsManager;
import org.septa.android.app.support.GeneralUtils;

import java.util.List;

public class TimeFrameItemAdapter extends RecyclerView.Adapter<TimeFrameItemAdapter.TimeFrameViewHolder> implements NotificationTimePickerDialogListener {

    private static final String TAG = TimeFrameItemAdapter.class.getSimpleName();

    private Activity activity;
    private TimeFrameItemListener mListener;
    private List<String> timeFrames;

    TimeFrameItemAdapter(Activity activity, List<String> timeFrames) {
        this.activity = activity;
        if (activity instanceof TimeFrameItemListener) {
            this.mListener = (TimeFrameItemListener) activity;
        } else {
            IllegalArgumentException iae = new IllegalArgumentException("Context Must Implement TimeFrameItemListener");
            CrashlyticsManager.log(Log.ERROR, TAG, iae.toString());
            throw iae;
        }
        this.timeFrames = timeFrames;
    }

    @NonNull
    @Override
    public TimeFrameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_timeframe, parent, false);
        return new TimeFrameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final TimeFrameViewHolder holder, final int position) {
        final String[] timeFrame = timeFrames.get(position).split(NotificationsSharedPrefsUtilsImpl.START_END_TIME_DELIM);

        // set timeframe number
        holder.timeFrameNumber.setText(activity.getString(R.string.notification_timeframe_number, position + 1));

        final int startTime = Integer.parseInt(timeFrame[0]);
        final int endTime = Integer.parseInt(timeFrame[1]);

        // set initial start and end time
        holder.startTimePicker.setText(GeneralUtils.getTimeFromInt(startTime));
        holder.endTimePicker.setText(GeneralUtils.getTimeFromInt(endTime));

        // start time picker
        holder.startTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = startTime / 100;
                int minute = startTime % 100;

                TimePickerDialog timePickerDialog = new TimePickerDialog(activity, TimeFrameItemAdapter.this, hour, minute, true, holder.getAdapterPosition());
                timePickerDialog.show();
            }
        });

        // end time picker
        holder.endTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = endTime / 100;
                int minute = endTime % 100;

                TimePickerDialog timePickerDialog = new TimePickerDialog(activity, TimeFrameItemAdapter.this, hour, minute, false, holder.getAdapterPosition());
                timePickerDialog.show();
            }
        });

        // delete button if 1+ time frames
        if (timeFrames.size() == 1) {
            holder.deleteButton.setVisibility(View.GONE);
        } else {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // delete time frame
                    List<String> timeFramesList = mListener.deleteTimeFrame(holder.getAdapterPosition());
                    updateList(timeFramesList);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return timeFrames.size();
    }

    @Override
    public void onStartTimeSet(TimePicker view, int hourOfDay, int minute, int position) {
        mListener.setStartTime(position, hourOfDay * 100 + minute);
    }

    @Override
    public void onEndTimeSet(TimePicker view, int hourOfDay, int minute, int position) {
        mListener.setEndTime(position, hourOfDay * 100 + minute);
    }

    public void updateList(final List<String> timeFramesList) {
        this.timeFrames = timeFramesList;
        notifyDataSetChanged();
    }

    class TimeFrameViewHolder extends RecyclerView.ViewHolder {
        LinearLayout container;
        ImageView deleteButton;
        TextView timeFrameNumber, startTimePicker, endTimePicker;

        TimeFrameViewHolder(final View view) {
            super(view);
            container = view.findViewById(R.id.item_notification_timeframe);
            timeFrameNumber = view.findViewById(R.id.timeframe_number);
            startTimePicker = view.findViewById(R.id.notification_schedule_start_picker);
            endTimePicker = view.findViewById(R.id.notification_schedule_end_picker);
            deleteButton = view.findViewById(R.id.timeframe_delete_button);
        }
    }

    interface TimeFrameItemListener {
        List<String> deleteTimeFrame(int position);

        void enableSaveButton();

        void setStartTime(int position, int newStartTime);

        void setEndTime(int position, int newEndTime);
    }

}