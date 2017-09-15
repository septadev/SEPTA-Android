package org.septa.android.app.schedules;

import android.content.Context;
import android.widget.BaseAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.septa.android.app.R;
import org.septa.android.app.domain.ScheduleItem;
import java.util.ArrayList;


/************************************************************************************************************
 * Class: SchedulesAdapter
 * Purpose: The Schedule adapter helper class that is used to bind to the custom List View that is created
 *          in the Schedules Fragment to display Transit Schedules. The Class extends the Base
 *          adapter and handles the view binding in the custom getView method.
 *
 * Created by ttuggerson on 8/31/17.
 */

public class SchedulesAdapter extends BaseAdapter {
        Context context;
        //String[] data;
        ArrayList<ScheduleItem> transitScheduleList = null;
        private static LayoutInflater inflater = null;

        public SchedulesAdapter(Context context, String[] data) {
            this.context = context;
            //this.data = data;
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }


        public SchedulesAdapter(Context context, ArrayList<ScheduleItem> scheduleList) {
            this.context = context;
            transitScheduleList = scheduleList;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount() {
            //returns count of data set
            //return data.length;
           return transitScheduleList.size();
        }

        @Override
        public Object getItem(int position) {
            // returns object data at selected position
            //return data[position];
            return transitScheduleList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        /*------------------------------------------------------------------------------------------
         * Method: getView
         * Purpose: Overridden getView method which inflates the "custom_row_for_schedules.xml" layout
         *          and binds the schedules data set to the custom row layout.
         *
         * Created by ttuggerson on 8/31/17.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (view == null)
                view = inflater.inflate(R.layout.custom_row_for_schedules, null);

            //get the departureTextView
            //bind resource data
            //----------------------------
            TextView text = (TextView) view.findViewById(R.id.departure_text);
            //text.setText("9:29 AM");
            text.setText(transitScheduleList.get(position).getDepartureTime(false));

            //get the duration;
            //bind resource
            //----------------------------
            TextView text2 = (TextView) view.findViewById(R.id.duration_text);
            //text2.setText("31m");
            text2.setText(transitScheduleList.get(position).getDuration());

            //get the arrival time
            //bind resource
            //----------------------------
            TextView text3 = (TextView) view.findViewById(R.id.arrival_text);
            //text3.setText("10:00 AM");
            text3.setText(transitScheduleList.get(position).getArrivalTime(false));

            return view;
        }
    }
