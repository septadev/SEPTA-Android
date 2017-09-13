package org.septa.android.app.schedules;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.database.DatabaseManager;
import org.septa.android.app.domain.ScheduleItem;
import android.widget.TextView;

import java.util.ArrayList;

/************************************************************************************************************
 * Class: SchedulesFragment
 * Purpose: The Schedule adapter class manages the Transit Schedules Fragment in the application
 * Created by ttuggerson on 8/31/17.
 */

public class SchedulesFragment extends Fragment {

    // private data members
    private ArrayList<ScheduleItem> scheduleItemArrayList = null;
    private DatabaseManager dbManager = null;
    private RadioGroup radioGroup = null;
    View layoutView = null;

    private String routeID = null;
    private String routeTitle = null;
    private String startingStop = null;
    private String destinationStop = null;
    private String routeDescription = null;
    private TransitType transitType = TransitType.TROLLEY;// default setting

    //----------------------------------------------------------------------------------------------
    //Method:  onCreateView
    //Purpose: initialize the dynamic views for the schedule fragment
    //
    //return void
    //----------------------------------------------------------------------------------------------
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        //-----------------------------------------------------------------------------------------
        // note to self
        // start the schedules activity
        // Step 1: pieces of information necessary for the schedules activity
        //         Transit Type
        //         Line ID
        //         Start Location
        //         Stop Location
        // Step 2: Query the database for the necessary schedule information
        // Step 3: Create Array Result set and then
        // Step 4: Inflate custom View and bind Data with list adapter
        // * Note: Will have to create a special case with the Rail line due to (M-TH) Fri Sat Sunday option
        // _________________________________________________________________________________________


        dbManager = DatabaseManager.getInstance(getActivity());
        layoutView = inflater.inflate(R.layout.schedules_main, null);

        setDebugLabelDefaults();// for debug only
        setRouteTitle(routeTitle);
        setRouteTitleDescrpition(routeDescription);
        setStartStation(startingStop);
        setDestinationStation(destinationStop);
        initRadioButtonGroup();

        //- below code is debug hook for db query and result set
        //-getDebugData will be replaced by actual query
       // ArrayList<ScheduleItem> scheduleItemArrayList = getDebugData(null);
        ArrayList<ScheduleItem> scheduleItemArrayList = querySchedules();
        bindListView(scheduleItemArrayList);


        return layoutView;
    }

    //----------------------------------------------------------------------------------------------
    //Method:  setRouteTitle
    //Purpose: initialize the dynamic text views for the schedule fragment
    //
    //return void
    //----------------------------------------------------------------------------------------------
    private void setRouteTitle (String routeName){
        //getView the set data
        TextView textView  =  (TextView) layoutView.findViewById(R.id.routeNameTextView);
        textView.setText(routeName);
    }
    private void setStartStation (String stopName){
        //getView the set data
        TextView textView = (TextView) layoutView.findViewById(R.id.startStationTextView);
        textView.setText(stopName);
    }

    private void setDestinationStation (String stopName){
        //getView the set data
        TextView textView = (TextView) layoutView.findViewById(R.id.destinationStationTextView);
        textView.setText(stopName);
    }

    private void setRouteTitleDescrpition(String descrpition){
        TextView textView = (TextView) layoutView.findViewById(R.id.routeDescriptionTextView);
        textView.setText(descrpition);
    }

    private void setTransitLineIndicator(String transitLine){

    }

    //----------------------------------------------------------------------------------------------
    //Method:  bindListView
    //Purpose: Method is responsible for binding the result schedule set to the custom
    //         listview in the SchedulesFragmemt - *important* this is the main view for this page
    //
    //return void
    //----------------------------------------------------------------------------------------------
    private void bindListView(ArrayList<ScheduleItem> itemslist){
        ListView ui_ListView = null;
        if(itemslist != null) {

            //get the schedule listview from the form
            //then bind the list of objects to the view in the ui
            //-----------------------------------------------------
            ui_ListView = (ListView) layoutView.findViewById(R.id.scheduleListView);
            ui_ListView.setAdapter(new SchedulesAdapter(layoutView.getContext(), itemslist));
        }
    }

    //----------------------------------------------------------------------------------------------
    //Method: initRadioButtonGroup
    //
    //Purpose: This is the main control for this form, this method is responsible for:
    //         1. ) setting up the group onchanged click event
    //              in the radio button group for the user control on the form.
    //
    //         2.) responsible for calling the "bindListView" method to rebind the view
    //         with the corresonding data from the UI Selection.
    //
    //         3.) Setting default selection state

    // Note:   It is important to note the "clearCheck" method must be called before the
    //         listener is invoked or event will not fire.
    //
    //         *IMPORTANT* Background and color change
    //         events and processing are done in the custom xml files listed below

    //         This method is used in conjuntion with five resource files:
    //         File: radio_btn_background_selector.xmlor.xml - handles selector for background change
    //               radio_btn_color_normalnormal.xml        - defines unselected button color
    //               radio_btn_color_selected.xmled.xml      - defines selected button background color
    //               radio_btn_font_color_selector.xml       - defines font colors for selection
    //               schedules_main.xml                      - main schedule UI fragment
    //-----------------------------------------------------------------------------------------------
    private void initRadioButtonGroup(){
        radioGroup = (RadioGroup) layoutView.findViewById(R.id.radioButtonGroup);
        radioGroup.clearCheck(); //must clear the defaults otherwise event wont fire
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedID) {
                try {

                    RadioButton rb = (RadioButton) radioGroup.findViewById(checkedID);
                    boolean checked = rb.isChecked();
                    // Check which radio button was clicked

                    switch (checkedID) {
                        case R.id.WeekDay_Button:
                            if (checked) {
                                //debug only - need to query database
                                bindListView(getDebugData(rb.getText().toString()));
                            }
                            break;
                        case R.id.Saturday_Button:
                            if (checked)
                                //debug only - need to query database
                                bindListView(querySchedules());
                                //bindListView(getDebugData(rb.getText().toString()));
                            break;
                        case R.id.Sunday_Button:
                            if (checked)
                                //debug only - need to query database
                                bindListView(getDebugData(rb.getText().toString()));
                            break;
                    }
                }catch(Exception ex){
                    System.out.print("Error Occured when binding data"+ ex.toString());
                }
            }
        });

        //set default settings for radio button
        RadioButton rb = (RadioButton) radioGroup.findViewById(R.id.WeekDay_Button);
        rb.setChecked(true);

    }

    //----------------------------------------------------------------------------------------------
    //Method:  getDebugData
    //Purpose: debug data creation method for view
    //
    //return ArrayList<ScheduleItem>
    //----------------------------------------------------------------------------------------------
    private ArrayList<ScheduleItem>  querySchedules(){

        //1. transit type
        //2. start station
        //3. Destination station
        //4. direction
        // will need to return an arraylist of <schedule items>
        ArrayList<ScheduleItem> scheduleItemArrayList = null;
        Cursor db_cursor = null;


        switch(transitType){
            case NHSL:
                //new db manager..
                break;
            case BUS:
                //new db manager..
                break;
            case TROLLEY:
                //new db manager..
                //this will either be table specific or class specific
                db_cursor = dbManager.getTrollySchedule()
                                     .getCursor(layoutView.getContext(), null);
                break;
            case SUBWAY:
                //new db manager..
                break;
            case RAIL:
                //new db manager..
                break;
        }

        //bind the db cursor data with the object model
        scheduleItemArrayList = bind_ScheduleList(db_cursor);

        return scheduleItemArrayList;
    }

    //----------------------------------------------------------------------------------------------
    //Method:  bind_ScheduleList
    //Purpose: method binds the db cursor data with the object model
    //
    //return ArrayList<ScheduleItem>
    //----------------------------------------------------------------------------------
    private ArrayList<ScheduleItem> bind_ScheduleList(Cursor cursor){
        ArrayList<ScheduleItem> scheduleItemArrayList = new  ArrayList<ScheduleItem>();


        //debug
        int i = 0;
        boolean endofCursor = false;

        try {
             endofCursor = cursor.moveToFirst();

            while (endofCursor != false) {

                ScheduleItem newScheduleItem = new ScheduleItem();
                newScheduleItem.setStopName("TestStop");
                newScheduleItem.setTripID(String.valueOf(cursor.getInt(0)));
                newScheduleItem.set_arrivalTime_24HrClock(String.valueOf(cursor.getInt(1)));
                newScheduleItem.setStopId(String.valueOf(cursor.getInt(2)));
                newScheduleItem.setStopSequence(cursor.getInt(3));

                scheduleItemArrayList.add(newScheduleItem);
                endofCursor = cursor.moveToNext();
                ++i;
                if(i>10)
                    break;
            }
            cursor.close();

        }catch(Exception ex){
            System.out.print("Object Access Exception"+ ex.toString());
        }

        return scheduleItemArrayList;

    }


    //----------------------------------------------------------------------------------------------
    //Method:  getDebugData
    //Purpose: debug data creation method for view
    //
    //return ArrayList<ScheduleItem>
    //----------------------------------------------------------------------------------------------
    //debug data population
    private ArrayList<ScheduleItem> getDebugData(String value){
        scheduleItemArrayList = new ArrayList<>();
        for (int i = 0; i <8; i++) {
            ScheduleItem newItem = new ScheduleItem("10:0"+i+"am", "10:3"+i+"am", value);
            scheduleItemArrayList.add(newItem);
        }
        return scheduleItemArrayList;
    }

    private void setDebugLabelDefaults(){
        routeTitle = "Paoli/Thorndale";
        startingStop = "Jefferson Station";
        destinationStop = "Paoli Station";
        routeDescription = "to/from Center City Pennslyvania";
    }
}

