/*
 * SchedulesItineraryActionBarActivity.java
 * Last modified on 05-12-2014 09:45-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities.schedules;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;
import org.septa.android.app.R;
import org.septa.android.app.activities.BaseAnalyticsActionBarActivity;
import org.septa.android.app.activities.FareInformationActionBarActivity;
import org.septa.android.app.activities.NextToArriveRealTimeWebViewActionBarActivity;
import org.septa.android.app.adapters.schedules.SchedulesItinerary_ListViewItem_ArrayAdapter;
import org.septa.android.app.adapters.schedules.Schedules_Itinerary_MenuDialog_ListViewItem_ArrayAdapter;
import org.septa.android.app.databases.SEPTADatabase;
import org.septa.android.app.managers.SchedulesFavoritesAndRecentlyViewedStore;
import org.septa.android.app.models.ObjectFactory;
import org.septa.android.app.models.RouteTypes;
import org.septa.android.app.models.SchedulesDataModel;
import org.septa.android.app.models.SchedulesFavoriteModel;
import org.septa.android.app.models.SchedulesRecentlyViewedModel;
import org.septa.android.app.models.SchedulesRouteModel;
import org.septa.android.app.models.SortOrder;
import org.septa.android.app.models.TripObject;
import org.septa.android.app.models.adapterhelpers.TextSubTextImageModel;
import org.septa.android.app.models.servicemodels.ServiceAdvisoryModel;
import org.septa.android.app.services.adaptors.AlertsAdaptor;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectViews;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static org.septa.android.app.models.RouteTypes.valueOf;

public class SchedulesItineraryActionBarActivity extends BaseAnalyticsActionBarActivity implements
        AdapterView.OnItemClickListener, StickyListHeadersListView.OnHeaderClickListener,
        StickyListHeadersListView.OnStickyHeaderOffsetChangedListener,
        StickyListHeadersListView.OnStickyHeaderChangedListener, View.OnTouchListener,
        Callback<ArrayList<ServiceAdvisoryModel>> {

    public static final String TAG = SchedulesItineraryActionBarActivity.class.getName();
    static final String SCHEDULE_MODEL = "scheduleModel";
    static final String SELECTED_TAB = "selectedTab";
    static final String IN_PROCESS = "inProcess";


    private SchedulesItinerary_ListViewItem_ArrayAdapter mAdapter;
    private boolean fadeHeader = true;

    private RouteTypes travelType;

    private StickyListHeadersListView stickyList;

    private ArrayList<SchedulesRouteModel> routesModel;

    private String iconImageNameSuffix;

    private final String[] tabLabels = new String[]{"NOW", "WEEKDAY", "SATURDAY", "SUNDAY"};
    private int selectedTab = 0;

    private boolean inProcessOfStartDestinationFlow;

    private SchedulesRouteModel schedulesRouteModel = new SchedulesRouteModel();

    private boolean menuRevealed = false;
    private ListView menuDialogListView;
    private Menu menu;

    private CountDownTimer schedulesItineraryRefreshCountDownTimer;
    private String actionBarTitleText;
    private List<ServiceAdvisoryModel> alerts;


    @InjectViews({ R.id.schedules_itinerary_tab_now_button
            , R.id.schedules_itinerary_tab_weekday_button
            , R.id.schedules_itinerary_tab_sat_button
            , R.id.schedules_itinerary_tab_sun_button })
    List<Button> tabs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedules_itinerary);
        ButterKnife.inject(this);

        actionBarTitleText = getIntent().getStringExtra(getString(R.string.actionbar_titletext_key));

        iconImageNameSuffix = getIntent().getStringExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key));
        String resourceName = getString(R.string.actionbar_iconimage_imagename_base).concat(iconImageNameSuffix);

        int id = getResources().getIdentifier(resourceName, "drawable", getPackageName());

        String stringTravelType = getIntent().getStringExtra(getString(R.string.schedules_itinerary_travelType));
        if (stringTravelType != null) {
            travelType = valueOf(getIntent().getStringExtra(getString(R.string.schedules_itinerary_travelType)));
        } else {
            Log.d("f", "travelType is null...");
        }

        if ((travelType == RouteTypes.TROLLEY) || (travelType == RouteTypes.BUS)) {
            actionBarTitleText = "Route " + actionBarTitleText;
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("| " + actionBarTitleText);
        getSupportActionBar().setIcon(id);

        this.inProcessOfStartDestinationFlow = false;

        mAdapter = new SchedulesItinerary_ListViewItem_ArrayAdapter(this, travelType);

        String schedulesRouteModelJSONString = getIntent().getStringExtra(getString(R.string.schedules_itinerary_schedulesRouteModel));
        Gson gson = new Gson();
        schedulesRouteModel = gson.fromJson(schedulesRouteModelJSONString, new TypeToken<SchedulesRouteModel>() {
        }.getType());


        String iconPrefix = getResources().getString(R.string.schedules_itinerary_menu_icon_imageBase);
        String[] texts = getResources().getStringArray(R.array.schedules_itinerary_menu_listview_items_texts);
        String[] subTexts = getResources().getStringArray(R.array.schedules_itinerary_menu_listview_items_subtexts);
        String[] iconSuffix = getResources().getStringArray(R.array.schedules_itinerary_menu_listview_items_iconsuffixes);
        TextSubTextImageModel[] listMenuItems = new TextSubTextImageModel[texts.length];
        for (int i = 0; i < texts.length; i++) {
            TextSubTextImageModel textSubTextImageModel = new TextSubTextImageModel(texts[i], subTexts[i], iconPrefix, iconSuffix[i]);
            listMenuItems[i] = textSubTextImageModel;
        }

        ListView menuListView = (ListView) findViewById(R.id.schedules_itinerary_menudialog_listview);
        this.menuDialogListView = menuListView;
        menuListView.setAdapter(new Schedules_Itinerary_MenuDialog_ListViewItem_ArrayAdapter(this, listMenuItems));


        stickyList = (StickyListHeadersListView) findViewById(R.id.list);
        stickyList.setOnItemClickListener(this);
        stickyList.setOnHeaderClickListener(this);
        stickyList.setOnStickyHeaderChangedListener(this);
        stickyList.setOnStickyHeaderOffsetChangedListener(this);
        stickyList.setEmptyView(findViewById(R.id.empty));
        stickyList.setDrawingListUnderStickyHeader(true);
        stickyList.setAreHeadersSticky(true);
        stickyList.setAdapter(mAdapter);
        stickyList.setOnTouchListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        routesModel = new ArrayList<SchedulesRouteModel>();

        if(savedInstanceState != null){
            schedulesRouteModel = savedInstanceState.getParcelable(SCHEDULE_MODEL);
            selectedTab = savedInstanceState.getInt(SELECTED_TAB);
            inProcessOfStartDestinationFlow = savedInstanceState.getBoolean(IN_PROCESS);
        }

        if (schedulesRouteModel != null && schedulesRouteModel.hasStartAndEndSelected()) {
            mAdapter.setRouteStartName(schedulesRouteModel.getRouteStartName());
            mAdapter.setRouteEndName(schedulesRouteModel.getRouteEndName());

        }

        AlertsAdaptor.getAlertsService().getAlertsForRoute
                (AlertsAdaptor.getServiceRouteName(schedulesRouteModel.getRouteShortName(), travelType), this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(SCHEDULE_MODEL, schedulesRouteModel);
        outState.putInt(SELECTED_TAB, selectedTab);
        outState.putBoolean(IN_PROCESS, inProcessOfStartDestinationFlow);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        View routeDirectionView = (View) findViewById(R.id.schedules_itinerary_routedirection_view);

        // get the color from the looking array given the ordinal position of the route type
        String color = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[travelType.ordinal()];
        routeDirectionView.setBackgroundColor(Color.parseColor(color));

        selectTab(selectedTab);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nexttoarrive_action_bar, menu);

        FrameLayout menuDialog = (FrameLayout) findViewById(R.id.schedules_itinerary_menudialog_mainlayout);
        menuDialog.setBackgroundResource(R.drawable.nexttoarrive_menudialog_bottomcorners);
        GradientDrawable drawable = (GradientDrawable) menuDialog.getBackground();
        drawable.setColor(0xFF353534);

        menuDialogListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case 0: {       // favorite
                        addOrRemoveRouteFromFavorites();
                        mAdapter.notifyDataSetChanged();
                        hideListView();
                        break;
                    }
                    case 1: {       // fare
                        startActivity(new Intent(SchedulesItineraryActionBarActivity.this,
                                FareInformationActionBarActivity.class));
                        hideListView();
                        break;
                    }
                    case 2: {       // service advisory
                        checkTripStartAndDestinationForNextToArriveDataRequest();
                        hideListView();
                        break;
                    }
                    case 3: {       // real time
                        startActivity(new Intent(SchedulesItineraryActionBarActivity.this,
                                NextToArriveRealTimeWebViewActionBarActivity.class));
                        hideListView();
                        break;
                    }
                }
            }
        });

        this.menu = menu;
        return true;
    }

    private void addOrRemoveRouteFromFavorites() {
        SchedulesFavoritesAndRecentlyViewedStore store = ObjectFactory.getInstance().getSchedulesFavoritesAndRecentlyViewedStore(this);

        SchedulesFavoriteModel schedulesFavoriteModel = new SchedulesFavoriteModel();
        schedulesFavoriteModel.setRouteId(schedulesRouteModel.getRouteId());
        schedulesFavoriteModel.setRouteStartStopId(schedulesRouteModel.getRouteStartStopId());
        schedulesFavoriteModel.setRouteStartName(schedulesRouteModel.getRouteStartName());
        schedulesFavoriteModel.setRouteEndStopId(schedulesRouteModel.getRouteEndStopId());
        schedulesFavoriteModel.setRouteEndName(schedulesRouteModel.getRouteEndName());
        schedulesFavoriteModel.setRouteShortName(schedulesRouteModel.getRouteShortName());

        // check if the selected route is already a favorite, then we allow the option of removing this
        // route from the favorites list.
        if (store.isFavorite(this.travelType.name(), schedulesFavoriteModel)) {
            Log.d("tt", "detected this is a favorite, remove ");
            store.removeFavorite(this.travelType.name(), schedulesFavoriteModel);
            ((Schedules_Itinerary_MenuDialog_ListViewItem_ArrayAdapter) menuDialogListView.getAdapter()).disableRemoveSavedFavorite();
            ((Schedules_Itinerary_MenuDialog_ListViewItem_ArrayAdapter) menuDialogListView.getAdapter()).enableSaveAsFavorite();
        } else {
            Log.d("tt", "adding as a favorite");
            store.addFavorite(this.travelType.name(), schedulesFavoriteModel);
            ((Schedules_Itinerary_MenuDialog_ListViewItem_ArrayAdapter) menuDialogListView.getAdapter()).disableSaveAsFavorite();
            ((Schedules_Itinerary_MenuDialog_ListViewItem_ArrayAdapter) menuDialogListView.getAdapter()).enableRemoveSavedFavorite();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionmenu_nexttoarrive_revealactions:

                if (menuRevealed) {

                    hideListView();
                } else {

                    revealListView();
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void revealListView() {
        menuRevealed = true;

        FrameLayout menuDialog = (FrameLayout) findViewById(R.id.schedules_itinerary_menudialog_mainlayout);
        ListView listView = (ListView) findViewById(R.id.schedules_itinerary_menudialog_listview);

        menuDialog.clearAnimation();

        listView.setVisibility(View.VISIBLE);
        menuDialog.setVisibility(View.VISIBLE);

        Animation mainLayoutAnimation = AnimationUtils.loadAnimation(this, R.anim.nexttoarrive_menudialog_scale_in);
        mainLayoutAnimation.setDuration(500);

        menuDialog.startAnimation(mainLayoutAnimation);
        listView.setVisibility(View.VISIBLE);
    }

    private void hideListView() {
        FrameLayout menuDialog = (FrameLayout) findViewById(R.id.schedules_itinerary_menudialog_mainlayout);
        menuDialog.clearAnimation();

        Animation mainLayOutAnimation = AnimationUtils.loadAnimation(this, R.anim.nexttoarrive_menudialog_scale_out);
        mainLayOutAnimation.setDuration(500);

        mainLayOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0) {
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                ListView listView = (ListView) findViewById(R.id.schedules_itinerary_menudialog_listview);
                listView.setVisibility(View.GONE);
                menuRevealed = false;
            }
        });

        menuDialog.startAnimation(mainLayOutAnimation);
    }

    public void startEndSelectionSelected(View view) {
        this.inProcessOfStartDestinationFlow = true;

        Intent stopSelectionIntent = null;
        int requestCode = 0;

        // for regional rail, we have only a single list of stops versus the two section list
        switch (travelType) {
            case RAIL: {
                stopSelectionIntent = new Intent(this, SchedulesRRStopSelectionActionBarActivity.class);
                requestCode = getResources().getInteger(R.integer.schedules_itinerary_stopselection_activityforresult_request_code_regionalrail);
                break;
            }
            default: {
                stopSelectionIntent = new Intent(this, SchedulesStopsSelectionActionBarActivity.class);

                stopSelectionIntent.putExtra(getString(R.string.actionbar_titletext_key), actionBarTitleText);
                stopSelectionIntent.putExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key), iconImageNameSuffix);
                stopSelectionIntent.putExtra(getString(R.string.schedules_itinerary_travelType),
                        travelType.name());
                stopSelectionIntent.putExtra(getString(R.string.schedules_itinerary_routeShortName), schedulesRouteModel.getRouteShortName());
                requestCode = getResources().getInteger(R.integer.schedules_itinerary_stopselection_activityforresult_request_code);
            }
        }

        stopSelectionIntent.putExtra(getString(R.string.schedules_stopselection_startordestination), "Start");
        startActivityForResult(stopSelectionIntent, requestCode);
    }

    public void selectDestinationSelected(View view) {
        Intent stopSelectionIntent = null;
        int requestCode = 0;

        // for regional rail, we have only a single list of stops versus the two section list
        switch (travelType) {
            case RAIL: {
                stopSelectionIntent = new Intent(this, SchedulesRRStopSelectionActionBarActivity.class);
                requestCode = getResources().getInteger(R.integer.schedules_itinerary_stopselection_activityforresult_request_code_regionalrail);
                break;
            }
            default: {
                stopSelectionIntent = new Intent(this, SchedulesStopsSelectionActionBarActivity.class);

                stopSelectionIntent.putExtra(getString(R.string.actionbar_titletext_key), actionBarTitleText);
                stopSelectionIntent.putExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key), iconImageNameSuffix);
                stopSelectionIntent.putExtra(getString(R.string.schedules_itinerary_travelType),
                        travelType.name());
                stopSelectionIntent.putExtra(getString(R.string.schedules_itinerary_routeShortName), schedulesRouteModel.getRouteShortName());
                requestCode = getResources().getInteger(R.integer.schedules_itinerary_stopselection_activityforresult_request_code);
            }
        }

        stopSelectionIntent.putExtra(getString(R.string.schedules_stopselection_startordestination), "Destination");
        startActivityForResult(stopSelectionIntent, requestCode);
    }

    public void reverseStartEndSelected(View view) {
        // Attempt to user the reverseStopSearch table to help plan the reverse trip
        if ((schedulesRouteModel.getRouteStartName() != null) &&
                !schedulesRouteModel.getRouteStartName().isEmpty() &&
                (schedulesRouteModel.getRouteEndName() != null) &&
                !schedulesRouteModel.getRouteEndName().isEmpty()) {
            try {
                SQLiteDatabase database = new SEPTADatabase(this).getReadableDatabase();
                String queryString = "SELECT reverseStopSearch.stop_id,reverse_stop_id,stop_name FROM " +
                        "reverseStopSearch JOIN stops_bus ON reverseStopSearch.reverse_stop_id=stops_bus.stop_id " +
                        "WHERE (reverseStopSearch.stop_id="+ schedulesRouteModel.getRouteStartStopId() +" " +
                        "OR reverseStopSearch.stop_id=" + schedulesRouteModel.getRouteEndStopId() + ") " +
                        "AND route_short_name='" + schedulesRouteModel.getRouteShortName() + "'";
                Cursor cursor = database.rawQuery(queryString, null);
                Log.d(TAG, "Reverse query: " + queryString);
                if(cursor != null && cursor.moveToFirst()) {
                    do {
                        String stopId = String.valueOf(cursor.getInt(0));
                        String reverseStopId = String.valueOf(cursor.getInt(1));
                        String stopName = cursor.getString(2);
                        if(schedulesRouteModel.getRouteStartStopId().equals(stopId)) {
                            schedulesRouteModel.setRouteStartName(stopName);
                            schedulesRouteModel.setRouteStartStopId(reverseStopId);
                        }else {
                            schedulesRouteModel.setRouteEndName(stopName);
                            schedulesRouteModel.setRouteEndStopId(reverseStopId);
                        }

                    } while(cursor.moveToNext());
                }
                cursor.close();
            } catch (Exception e) {
                Log.e(TAG, "Reverse query failed", e);
                schedulesRouteModel.reverseStartAndDestinationStops();
            }

        } else {
            schedulesRouteModel.reverseStartAndDestinationStops();
        }
        SchedulesItinerary_ListViewItem_ArrayAdapter schedulesListViewItemArrayAdapter = (SchedulesItinerary_ListViewItem_ArrayAdapter) stickyList.getAdapter();
        schedulesListViewItemArrayAdapter.setRouteStartName(schedulesRouteModel.getRouteStartName());
        schedulesListViewItemArrayAdapter.setRouteEndName(schedulesRouteModel.getRouteEndName());

        checkTripStartAndDestinationForNextToArriveDataRequest();
    }

    private void checkTripStartAndDestinationForNextToArriveDataRequest() {
        boolean flipStartAndEndStops = false;
        Log.d(TAG, "check trip start and destination");
        // check if we have both the start and destination stops, if yes, fetch the data.
        if ((schedulesRouteModel.getRouteStartName() != null) &&
                !schedulesRouteModel.getRouteStartName().isEmpty() &&
                (schedulesRouteModel.getRouteEndName() != null) &&
                !schedulesRouteModel.getRouteEndName().isEmpty()) {

            ListView menuListView = (ListView) findViewById(R.id.schedules_itinerary_menudialog_listview);

            ((Schedules_Itinerary_MenuDialog_ListViewItem_ArrayAdapter) menuListView.getAdapter()).enableSaveAsFavorite();
            SchedulesDataModel schedulesDataModel = new SchedulesDataModel(this);

            if (schedulesItineraryRefreshCountDownTimer != null) {
                schedulesItineraryRefreshCountDownTimer.cancel();
                schedulesItineraryRefreshCountDownTimer.start();
            } else {
                schedulesItineraryRefreshCountDownTimer = createScheduleItineraryRefreshCountDownTimer();
                schedulesItineraryRefreshCountDownTimer.start();
            }

            schedulesDataModel.setRoute(schedulesRouteModel);
            schedulesDataModel.loadStartBasedTrips(travelType);
            flipStartAndEndStops = schedulesDataModel.loadAndProcessEndStopsWithStartStops(travelType);

            if (flipStartAndEndStops) {
                schedulesRouteModel.reverseStartAndDestinationStops();
            }

            // given the schedules route model, create a recently viewed and favorite model
            SchedulesFavoritesAndRecentlyViewedStore store = ObjectFactory.getInstance().getSchedulesFavoritesAndRecentlyViewedStore(this);

            SchedulesRecentlyViewedModel schedulesRecentlyViewedModel = new SchedulesRecentlyViewedModel();
            schedulesRecentlyViewedModel.setRouteId(schedulesRouteModel.getRouteId());
            schedulesRecentlyViewedModel.setRouteStartName(schedulesRouteModel.getRouteStartName());
            schedulesRecentlyViewedModel.setRouteStartStopId(schedulesRouteModel.getRouteStartStopId());
            schedulesRecentlyViewedModel.setRouteEndName(schedulesRouteModel.getRouteEndName());
            schedulesRecentlyViewedModel.setRouteEndStopId(schedulesRouteModel.getRouteEndStopId());
            schedulesRecentlyViewedModel.setRouteShortName(schedulesRouteModel.getRouteShortName());
            schedulesRecentlyViewedModel.setRouteShortName(schedulesRouteModel.getRouteShortName());

            SchedulesFavoriteModel schedulesFavoriteModel = new SchedulesFavoriteModel();
            schedulesFavoriteModel.setRouteId(schedulesRouteModel.getRouteId());
            schedulesFavoriteModel.setRouteStartStopId(schedulesRouteModel.getRouteStartStopId());
            schedulesFavoriteModel.setRouteStartName(schedulesRouteModel.getRouteStartName());
            schedulesFavoriteModel.setRouteEndStopId(schedulesRouteModel.getRouteEndStopId());
            schedulesFavoriteModel.setRouteEndName(schedulesRouteModel.getRouteEndName());
            schedulesFavoriteModel.setRouteShortName(schedulesRouteModel.getRouteShortName());

            // check if the selected route is already a favorite, then we allow the option of removing this
            // route from the favorites list.
            if (store.isFavorite(this.travelType.name(), schedulesFavoriteModel)) {
                ((Schedules_Itinerary_MenuDialog_ListViewItem_ArrayAdapter) menuListView.getAdapter()).enableRemoveSavedFavorite();
                ((Schedules_Itinerary_MenuDialog_ListViewItem_ArrayAdapter) menuListView.getAdapter()).disableSaveAsFavorite();
            }

            // check if this route is already stored as a favorite; if not store as a recent
            if (!store.isFavorite(this.travelType.name(), schedulesRecentlyViewedModel)) {
                store.addRecentlyViewed(this.travelType.name(), schedulesRecentlyViewedModel);
            }

            Log.d(TAG, "route short name and travel type " + schedulesRouteModel.getRouteShortName() + " " + travelType);
            switch (travelType) {
                case RAIL: {
                    TextView endRouteNameTextView = (TextView) findViewById(R.id.schedules_itinerary_routedirection_textview);
                    endRouteNameTextView.setText("To " + schedulesRouteModel.getRouteEndName());

                    break;
                }
                case BUS:
                case BSL:
                case MFL:
                case NHSL:
                case TROLLEY: {
                    Log.d(TAG, "load the direction header from SQLite");
                    DirectionHeaderLoader directionHeaderLoader = new DirectionHeaderLoader(schedulesRouteModel.getRouteShortName());
                    directionHeaderLoader.execute(schedulesDataModel);

                    break;
                }
            }

            ArrayList<TripObject> trips = schedulesDataModel.createFilteredTripsList(selectedTab);
            mAdapter.setTripObject(trips);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == getResources().getInteger(R.integer.schedules_itinerary_stopselection_activityforresult_request_code_regionalrail)) {
            if (resultCode == RESULT_OK) {
                String stopName = data.getStringExtra("stop_name");
                String stopId = data.getStringExtra("stop_id");
                String selectionMode = data.getStringExtra("selection_mode");

                if (selectionMode.equals("destination")) {
                    schedulesRouteModel.setRouteEndStopId(stopId);
                    schedulesRouteModel.setRouteEndName(stopName);

                    if (inProcessOfStartDestinationFlow) {
                        inProcessOfStartDestinationFlow = false;
                    }
                } else {
                    schedulesRouteModel.setRouteStartStopId(stopId);
                    schedulesRouteModel.setRouteStartName(stopName);

                    if (inProcessOfStartDestinationFlow) {
                        inProcessOfStartDestinationFlow = false;
                        Intent stopSelectionIntent = new Intent(this, SchedulesRRStopSelectionActionBarActivity.class);

                        stopSelectionIntent.putExtra(getString(R.string.schedules_stopselection_startordestination), "Destination");

                        startActivityForResult(stopSelectionIntent, getResources().getInteger(R.integer.schedules_itinerary_stopselection_activityforresult_request_code_regionalrail));
                    }
                }

                SchedulesItinerary_ListViewItem_ArrayAdapter schedulesListViewItemArrayAdapter = (SchedulesItinerary_ListViewItem_ArrayAdapter) stickyList.getAdapter();
                schedulesListViewItemArrayAdapter.setRouteStartName(schedulesRouteModel.getRouteStartName());
                schedulesListViewItemArrayAdapter.setRouteEndName(schedulesRouteModel.getRouteEndName());

                checkTripStartAndDestinationForNextToArriveDataRequest();
            } else {
                if (resultCode == RESULT_CANCELED) {
                    Log.d(TAG, "the result is canceled");
                    //Write your code if there's no result
                }
            }
        } else {
            if (requestCode == getResources().getInteger(R.integer.schedules_itinerary_stopselection_activityforresult_request_code)) {
                if (resultCode == RESULT_OK) {
                    String stopName = data.getStringExtra("stop_name");
                    String stopId = data.getStringExtra("stop_id");
                    String selectionMode = data.getStringExtra("selection_mode");

                    if (selectionMode.equals("Destination")) {
                        Log.d(TAG, "selection mode is detintation, right? " + selectionMode);
                        schedulesRouteModel.setRouteEndStopId(stopId);
                        schedulesRouteModel.setRouteEndName(stopName);

                        if (inProcessOfStartDestinationFlow) {
                            Log.d(TAG, "inproces was true");
                            inProcessOfStartDestinationFlow = false;
                        }
                    } else {
                        Log.d(TAG, "not in selection mode of desintation as " + selectionMode);
                        schedulesRouteModel.setRouteStartStopId(stopId);
                        schedulesRouteModel.setRouteStartName(stopName);

                        if (inProcessOfStartDestinationFlow) {
                            Log.d(TAG, "inprocess is true here");
                            inProcessOfStartDestinationFlow = false;
                            Intent stopSelectionIntent = new Intent(this, SchedulesStopsSelectionActionBarActivity.class);

                            stopSelectionIntent.putExtra(getString(R.string.actionbar_titletext_key), actionBarTitleText);
                            stopSelectionIntent.putExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key), iconImageNameSuffix);
                            stopSelectionIntent.putExtra(getString(R.string.schedules_itinerary_travelType),
                                    travelType.name());
                            stopSelectionIntent.putExtra(getString(R.string.schedules_itinerary_routeShortName), schedulesRouteModel.getRouteShortName());
                            stopSelectionIntent.putExtra(getString(R.string.schedules_stopselection_startordestination), "Destination");
                            if(data.hasExtra(getString(R.string.schedules_stopselection_sort_order))) {
                                SortOrder sortOrder = (SortOrder)data.getSerializableExtra(getString(R.string.schedules_stopselection_sort_order));
                                stopSelectionIntent.putExtra(getString(R.string.schedules_stopselection_sort_order), sortOrder);
                            }

                            startActivityForResult(stopSelectionIntent, getResources().getInteger(R.integer.schedules_itinerary_stopselection_activityforresult_request_code));
                        }
                    }

                    SchedulesItinerary_ListViewItem_ArrayAdapter schedulesListViewItemArrayAdapter = (SchedulesItinerary_ListViewItem_ArrayAdapter) stickyList.getAdapter();
                    schedulesListViewItemArrayAdapter.setRouteStartName(schedulesRouteModel.getRouteStartName());
                    schedulesListViewItemArrayAdapter.setRouteEndName(schedulesRouteModel.getRouteEndName());

                    checkTripStartAndDestinationForNextToArriveDataRequest();
                } else {
                    if (resultCode == RESULT_CANCELED) {
                        Log.d(TAG, "the result is canceled");
                        //Write your code if there's no result
                    }
                }
            }
        }
    }

    public void tabSelected(View view) {
        switch (view.getId()) {
            case R.id.schedules_itinerary_tab_weekday_button: {
                Log.d(TAG, "heard the tab selected: weekday" );
                selectTab(1);

                break;
            }
            case R.id.schedules_itinerary_tab_sat_button: {
                Log.d(TAG, "heard the tab selected: sat" );
                selectTab(2);

                break;
            }
            case R.id.schedules_itinerary_tab_sun_button: {
                Log.d(TAG, "heard the tab selected: sun" );
                selectTab(3);
                break;
            }
            default: {
                selectTab(0);
            }
        }
    }

    public void selectTab(int index){
        selectedTab = index;
        setTabSelected(tabs.get(selectedTab));

        for(int i = 0 ; i < tabs.size(); i++){
            if(i != selectedTab){
                setTabUnselected(tabs.get(i));
            }
        }

        switch (index){

            case 1:{
                mAdapter.setHeaderViewText(tabLabels[selectedTab]);
                break;
            }
            case 2:{
                mAdapter.setHeaderViewText(tabLabels[selectedTab]);
                break;
            }
            case 3:{
                mAdapter.setHeaderViewText(tabLabels[selectedTab]);
                break;
            } default:{
                mAdapter.setHeaderViewText("REMAINING TRIPS FOR TODAY");
            }
        }

        mAdapter.setSelectedTab(selectedTab);
        checkTripStartAndDestinationForNextToArriveDataRequest();
    }

    private void setTabSelected(Button button){
        String color = this.getResources().getStringArray(R.array.schedules_routeselection_routesheader_solid_colors)[travelType.ordinal()];
        GradientDrawable gradientDrawable = (GradientDrawable)button.getBackground();

        gradientDrawable.setColor(Color.parseColor(color));
        button.setTextColor(Color.WHITE);
    }

    private void setTabUnselected(Button button){
        GradientDrawable gradientDrawable = (GradientDrawable)button.getBackground();
        gradientDrawable.setColor(Color.LTGRAY);
        button.setTextColor(Color.BLACK);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public void onHeaderClick(StickyListHeadersListView l, View header,
                              int itemPosition, long headerId, boolean currentlySticky) {

    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onStickyHeaderOffsetChanged(StickyListHeadersListView l, View header, int offset) {
        if (fadeHeader && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            header.setAlpha(1 - (offset / (float) header.getMeasuredHeight()));
        }
    }

    @Override
    public void onStickyHeaderChanged(StickyListHeadersListView l, View header,
                                      int itemPosition, long headerId) {

    }

    @Override
    protected void onPause() {
        super.onPause();

        schedulesItineraryRefreshCountDownTimer.cancel();
    }

    @Override
    protected void onStart() {
        super.onStart();

        schedulesItineraryRefreshCountDownTimer = createScheduleItineraryRefreshCountDownTimer();
        if ((schedulesRouteModel.getRouteStartName() != null) &&
                !schedulesRouteModel.getRouteStartName().isEmpty() &&
                (schedulesRouteModel.getRouteEndName() != null) &&
                !schedulesRouteModel.getRouteEndName().isEmpty()) {
            schedulesItineraryRefreshCountDownTimer.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        schedulesItineraryRefreshCountDownTimer = null;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.setOnTouchListener(null);
        return false;
    }

    private CountDownTimer createScheduleItineraryRefreshCountDownTimer() {
        return new CountDownTimer(20000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

//                ((Schedules_Itinerary_MenuDialog_ListViewItem_ArrayAdapter)menuDialogListView.getAdapter()).setNextRefreshInSecondsValue(millisUntilFinished/1000);
            }

            @Override
            public void onFinish() {

                checkTripStartAndDestinationForNextToArriveDataRequest();
            }
        };
    }

    private void setMenuDetour() {
        MenuItem item = menu.findItem(R.id.actionmenu_nexttoarrive_revealactions);
        AnimationDrawable animationDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.actionmenu_detour_animation);
        item.setIcon(animationDrawable);
        animationDrawable.start();
    }

    private void setMenuAdvisory() {
        MenuItem item = menu.findItem(R.id.actionmenu_nexttoarrive_revealactions);
        AnimationDrawable animationDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.actionmenu_advisory_animation);
        item.setIcon(animationDrawable);
        animationDrawable.start();
    }

    private void setMenuDetourAdvisory() {
        MenuItem item = menu.findItem(R.id.actionmenu_nexttoarrive_revealactions);
        AnimationDrawable animationDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.actionmenu_detour_advisory_animation);
        item.setIcon(animationDrawable);
        animationDrawable.start();
    }

    @Override
    public void success(ArrayList<ServiceAdvisoryModel> serviceAdvisoryModels, Response response) {
        if(response.getStatus() == HttpStatus.SC_OK && serviceAdvisoryModels != null) {
            alerts = serviceAdvisoryModels;
            if(ServiceAdvisoryModel.hasValidAdvisory(alerts)
                    && ServiceAdvisoryModel.hasValidDetours(alerts)) {
                setMenuDetourAdvisory();
            } else if(ServiceAdvisoryModel.hasValidDetours(alerts)) {
                setMenuDetour();
            } else if(ServiceAdvisoryModel.hasValidAdvisory(alerts)) {
                setMenuAdvisory();
            }
        }
    }

    @Override
    public void failure(RetrofitError error) {

    }

    private class DirectionHeaderLoader extends AsyncTask<SchedulesDataModel, Integer, Boolean> {
        String routeShortName;
        SchedulesDataModel schedulesDataModel;
        String directionHeaderString;

        public DirectionHeaderLoader(String routeShortName) {

            this.routeShortName = routeShortName;
        }

        private void loadDirectionHeaders(SchedulesDataModel schedulesDataModel) {
            String queryString;

            this.schedulesDataModel = schedulesDataModel;

            SEPTADatabase septaDatabase = new SEPTADatabase(SchedulesItineraryActionBarActivity.this);
            SQLiteDatabase database = septaDatabase.getReadableDatabase();

            if (routeShortName != null) {
                queryString = "SELECT dircode, Route, DirectionDescription FROM bus_stop_directions WHERE Route=\"%%route_short_name%%\" ORDER BY dircode";
                queryString = queryString.replace("%%route_short_name%%", routeShortName);

                Cursor cursor = null;

                if (queryString != null) {
                    cursor = database.rawQuery(queryString, null);
                }

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            if (cursor.getInt(0) == schedulesDataModel.getCurrentDisplayDirection()) {
                                Log.d("p", "found the direction code, will set string");
                                directionHeaderString = cursor.getString(2);
                            }
                        } while (cursor.moveToNext());
                    }

                    cursor.close();
                } else {
                    Log.d("f", "cursor is null");
                }

                database.close();
            }

        }

        @Override
        protected Boolean doInBackground(SchedulesDataModel... params) {
            SchedulesDataModel schedulesDataModel = params[0];

            loadDirectionHeaders(schedulesDataModel);

            return false;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);

            TextView endRouteNameTextView = (TextView) findViewById(R.id.schedules_itinerary_routedirection_textview);
            endRouteNameTextView.setText("To " + directionHeaderString);
        }
    }
}