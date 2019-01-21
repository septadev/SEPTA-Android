package org.septa.android.app.schedules;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.septa.android.app.ActivityClass;
import org.septa.android.app.BaseActivity;
import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.database.DatabaseManager;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.domain.ScheduleModel;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.favorites.DeleteFavoritesAsyncTask;
import org.septa.android.app.favorites.edit.RenameFavoriteDialogFragment;
import org.septa.android.app.favorites.edit.RenameFavoriteListener;
import org.septa.android.app.nextarrive.NextToArriveResultsActivity;
import org.septa.android.app.notifications.NotificationsManagementFragment;
import org.septa.android.app.notifications.PushNotificationManager;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Alert;
import org.septa.android.app.services.apiinterfaces.model.Favorite;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalFavorite;
import org.septa.android.app.support.AnalyticsManager;
import org.septa.android.app.support.CrashlyticsManager;
import org.septa.android.app.support.Criteria;
import org.septa.android.app.support.CursorAdapterSupplier;
import org.septa.android.app.support.GeneralUtils;
import org.septa.android.app.systemstatus.GoToSystemStatusResultsOnClickListener;
import org.septa.android.app.systemstatus.SystemStatusState;
import org.septa.android.app.view.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.septa.android.app.favorites.edit.RenameFavoriteDialogFragment.EDIT_FAVORITE_DIALOG_KEY;

public class ScheduleResultsActivity extends BaseActivity implements RenameFavoriteListener {

    private static final String TAG = ScheduleResultsActivity.class.getSimpleName();

    private ActivityClass originClass = ActivityClass.SCHEDULES;
    private static final int RAIL_MON_THUR = 8;
    private static final int WEEK_DAY = 32;
    private static final int RAIL_FRIDAY = 2;
    private static final int RAIL_SATURDAY = 1;
    private static final int SATURDAY = 1;
    private static final int RAIL_SUNDAY = 64;
    private static final int SUNDAY = 64;

    private RadioGroup radioGroup = null;
    CursorAdapterSupplier<ScheduleModel> scheduleCursorAdapterSupplier;
    CursorAdapterSupplier<StopModel> reverseStopCursorAdapterSupplier;

    StopModel start = null;
    StopModel destination = null;
    RouteDirectionModel routeDirectionModel;
    TransitType transitType;
    CursorAdapterSupplier<RouteDirectionModel> reverseRouteCursorAdapterSupplier;
    NextArrivalFavorite currentFavorite;
    Menu menu;

    // layout variables
    View lineStationLayout;
    TextView startStationText;
    TextView destinationTextView;
    View reverseTripLabel;
    ImageView alertView;
    ImageView advisoryView;
    ImageView detourView;
    ImageView weatherView;
    SwitchCompat notifsSwitch;
    ListView scheduleResultsListView;
    Snackbar snackbar;

    // used to ignore notif toggles due to failed subscription POSTs
    private boolean ignoreSwitch = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Schedules");

        setContentView(R.layout.activity_schedules_results);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        } else {
            restoreState(getIntent().getExtras());
        }

        if (transitType == TransitType.RAIL) {
            scheduleCursorAdapterSupplier = DatabaseManager.getInstance(this).getRailScheduleCursorAdapterSupplier();
            reverseRouteCursorAdapterSupplier = DatabaseManager.getInstance(this).getRailRouteCursorAdapterSupplier();
        } else {
            scheduleCursorAdapterSupplier = DatabaseManager.getInstance(this).getNonRegionalRailScheduleCursorAdapterSupplier();
            reverseStopCursorAdapterSupplier = DatabaseManager.getInstance(this).getNonRailReverseAdapterSupplier();
            reverseRouteCursorAdapterSupplier = DatabaseManager.getInstance(this).getNonRailReverseRouteCursorAdapterSupplier();
        }

        startStationText = findViewById(R.id.start_station_text);
        destinationTextView = findViewById(R.id.destination_station_text);

        reverseTripLabel = findViewById(R.id.button_reverse_schedule_trip);
        reverseTripLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReverseStopAsyncTask reverseStopAsyncTask = new ReverseStopAsyncTask(ScheduleResultsActivity.this);
                reverseStopAsyncTask.execute();
            }
        });

        setUpHeaders();

        lineStationLayout = findViewById(R.id.line_station_layout);
        lineStationLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                startStationText.setRight(reverseTripLabel.getLeft());
                startStationText.setText(startStationText.getText());
                destinationTextView.setRight(reverseTripLabel.getLeft());
                destinationTextView.setText(destinationTextView.getText());
            }
        });

        initRadioButtonGroup();

        scheduleResultsListView = findViewById(R.id.schedule_list_view);

        View ntaLink = findViewById(R.id.nta_link);
        ntaLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScheduleResultsActivity.this, NextToArriveResultsActivity.class);
                intent.putExtra(Constants.STARTING_STATION, start);
                intent.putExtra(Constants.DESTINATION_STATION, destination);
                intent.putExtra(Constants.TRANSIT_TYPE, transitType);
                intent.putExtra(Constants.ROUTE_DIRECTION_MODEL, routeDirectionModel);

                AnalyticsManager.logContentViewEvent(TAG, AnalyticsManager.CONTENT_VIEW_EVENT_NTA_FROM_SCHEDULE, AnalyticsManager.CONTENT_ID_NEXT_TO_ARRIVE, null);
                startActivityForResult(intent, Constants.NTA_REQUEST);
            }
        });

        setUpAlertsView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (notifsSwitch != null && notifsSwitch.isChecked()) {
            checkIfPushNotifsDisabled();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // remove message
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(Constants.DESTINATION_STATION, destination);
        outState.putSerializable(Constants.STARTING_STATION, start);
        outState.putSerializable(Constants.TRANSIT_TYPE, transitType);
        outState.putSerializable(Constants.ROUTE_DIRECTION_MODEL, routeDirectionModel);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        invalidateOptionsMenu();

        this.menu = menu;
        getMenuInflater().inflate(R.menu.favorite_menu, menu);

        if (currentFavorite != null) {
            menu.findItem(R.id.create_favorite).setIcon(R.drawable.ic_favorite_made);
            menu.findItem(R.id.create_favorite).setTitle(R.string.schedule_favorite_icon_title_remove);
        } else {
            menu.findItem(R.id.create_favorite).setTitle(R.string.schedule_favorite_icon_title_create);
        }

        // hide refresh icon -- refresh only needed in NTA Results Activity
        menu.findItem(R.id.refresh_results).setVisible(false);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_favorite:
                saveAsFavorite(item);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int unmaskedRequestCode = requestCode & 0x0000ffff;

        if (unmaskedRequestCode == Constants.NTA_REQUEST || unmaskedRequestCode == Constants.SYSTEM_STATUS_REQUEST) {
            if (resultCode == Constants.VIEW_NOTIFICATION_MANAGEMENT) {
                goToNotificationsManagement();
            }
        }
    }

    @Override
    public void updateFavorite(Favorite favorite) {
        if (favorite instanceof NextArrivalFavorite) {
            currentFavorite = (NextArrivalFavorite) favorite;
            renameFavorite(currentFavorite);

            Toast.makeText(ScheduleResultsActivity.this, R.string.create_nta_favorite, Toast.LENGTH_LONG).show();
        } else {
            Log.e(TAG, "Attempted to save invalid Favorite type");
        }
    }

    @Override
    public void renameFavorite(Favorite favorite) {
        invalidateOptionsMenu();
    }

    @Override
    public void favoriteCreationFailed() {
        Toast.makeText(ScheduleResultsActivity.this, R.string.create_nta_favorite_failed, Toast.LENGTH_LONG).show();
    }

    private void restoreState(Bundle bundle) {
        destination = (StopModel) bundle.get(Constants.DESTINATION_STATION);
        start = (StopModel) bundle.get(Constants.STARTING_STATION);
        transitType = (TransitType) bundle.get(Constants.TRANSIT_TYPE);
        routeDirectionModel = (RouteDirectionModel) bundle.get(Constants.ROUTE_DIRECTION_MODEL);
    }

    private void initRadioButtonGroup() {
        radioGroup = findViewById(R.id.day_of_week_button_group);
        radioGroup.clearCheck(); //must clear the defaults otherwise event wont fire
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedID) {
                RadioButton rb = radioGroup.findViewById(checkedID);
                if (rb.isChecked()) {
                    ScheduleResultsAsyncTask task = new ScheduleResultsAsyncTask(ScheduleResultsActivity.this);
                    task.execute(mapRadioButtonIdtoSchedule(checkedID, routeDirectionModel.getRouteId()));
                }
            }
        });


        if (transitType == TransitType.RAIL) {
            findViewById(R.id.weekday_button).setVisibility(View.GONE);
            findViewById(R.id.mon_thurs_button).setVisibility(View.VISIBLE);
            findViewById(R.id.friday_button).setVisibility(View.VISIBLE);
            RadioButton rb = radioGroup.findViewById(R.id.mon_thurs_button);
            rb.setChecked(true);
        } else

        {
            RadioButton rb = radioGroup.findViewById(R.id.weekday_button);
            rb.setChecked(true);
        }

    }

    private void setUpHeaders() {
        TextView routeNameTextView = findViewById(R.id.route_name_text);
        routeNameTextView.setText(routeDirectionModel.getRouteLongName());

        TextView routeTitleDescription = findViewById(R.id.route_description_text);
        if (transitType == TransitType.RAIL) {
            routeTitleDescription.setText(routeDirectionModel.getDirectionDescription());
        } else {
            routeTitleDescription.setText("to " + routeDirectionModel.getDirectionDescription());
        }

        startStationText.setText(start.getStopName());
        destinationTextView.setText(destination.getStopName());

        ImageView transitTypeImageView = findViewById(R.id.transit_type_image);
        transitTypeImageView.setImageResource(transitType.getIconForLine(routeDirectionModel.getRouteId(), this));

        String favKey = NextArrivalFavorite.generateKey(start, destination, transitType, routeDirectionModel);
        // currentFavorite can be null if the current selection is not a favorite
        currentFavorite = (NextArrivalFavorite) SeptaServiceFactory.getFavoritesService().getFavoriteByKey(this, favKey);

        // check if already a favorite
        if (menu != null) {
            if (currentFavorite != null) {
                menu.findItem(R.id.create_favorite).setIcon(R.drawable.ic_favorite_made);
            } else {
                menu.findItem(R.id.create_favorite).setIcon(R.drawable.ic_favorite_available);
            }
        }
    }

    private int mapRadioButtonIdtoSchedule(int checkedID, String routeId) {
        switch (checkedID) {
            case R.id.weekday_button:
                return WEEK_DAY;

            case R.id.mon_thurs_button:
                return RAIL_MON_THUR;

            case R.id.friday_button:
                return RAIL_FRIDAY;

            case R.id.saturday_button:
                if (transitType == TransitType.RAIL) {
                    return RAIL_SATURDAY;
                } else {
                    return SATURDAY;
                }

            case R.id.sunday_button:
                if (transitType == TransitType.RAIL) {
                    return RAIL_SUNDAY;
                } else {
                    return SUNDAY;
                }
        }
        return 0;
    }

    public void saveAsFavorite(final MenuItem item) {
        if (!item.isEnabled()) {
            return;
        }

        item.setEnabled(false);

        if (start != null && destination != null && transitType != null) {
            if (currentFavorite == null) {
                // prompt to save favorite
                final NextArrivalFavorite nextArrivalFavorite = new NextArrivalFavorite(start, destination, transitType, routeDirectionModel);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                CrashlyticsManager.log(Log.INFO, TAG, "Creating initial RenameFavoriteDialogFragment for:" + nextArrivalFavorite.toString());
                RenameFavoriteDialogFragment fragment = RenameFavoriteDialogFragment.newInstance(false, false, nextArrivalFavorite);
                fragment.show(ft, EDIT_FAVORITE_DIALOG_KEY);

                item.setEnabled(true);

            } else {
                new AlertDialog.Builder(this).setCancelable(true).setTitle(R.string.delete_fav_modal_title)
                        .setMessage(R.string.delete_fav_modal_text)
                        .setPositiveButton(R.string.delete_fav_pos_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DeleteFavoritesAsyncTask task = new DeleteFavoritesAsyncTask(ScheduleResultsActivity.this, new Runnable() {
                                    @Override
                                    public void run() {
                                        item.setEnabled(true);
                                    }
                                }, new Runnable() {
                                    @Override
                                    public void run() {
                                        item.setEnabled(true);
                                        item.setIcon(R.drawable.ic_favorite_available);
                                        currentFavorite = null;
                                    }
                                });

                                AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_DELETE_FAVORITE, AnalyticsManager.CUSTOM_EVENT_ID_FAVORITES_MANAGEMENT, null);

                                task.execute(currentFavorite.getKey());
                            }
                        }).setNegativeButton(R.string.delete_fav_neg_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        item.setEnabled(true);
                    }
                }).create().show();
            }

        } else {
            item.setEnabled(true);
        }
    }

    private void setUpAlertsView() {
        Alert alert = SystemStatusState.getAlertForLine(transitType, routeDirectionModel.getRouteId());

        alertView = findViewById(R.id.alert_icon);
        advisoryView = findViewById(R.id.advisory_icon);
        detourView = findViewById(R.id.detour_icon);
        weatherView = findViewById(R.id.weather_icon);
        notifsSwitch = findViewById(R.id.notification_route_switch);

        if (alert.isAdvisory()) {
            advisoryView.setImageResource(R.drawable.ic_advisory);
            advisoryView.setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.SERVICE_ADVISORY_EXPANDED, ScheduleResultsActivity.this, transitType, routeDirectionModel.getRouteId(), routeDirectionModel.getRouteShortName(), originClass));
        } else {
            advisoryView.setImageResource(R.drawable.ic_advisory_inactive);
        }

        if (alert.isSuspended()) {
            alertView.setImageResource(R.drawable.ic_suspension);
            alertView.setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.SERVICE_ALERT_EXPANDED, ScheduleResultsActivity.this, transitType, routeDirectionModel.getRouteId(), routeDirectionModel.getRouteShortName(), originClass));
        } else if (alert.isAlert()) {
            alertView.setImageResource(R.drawable.ic_alert);
            alertView.setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.SERVICE_ALERT_EXPANDED, ScheduleResultsActivity.this, transitType, routeDirectionModel.getRouteId(), routeDirectionModel.getRouteShortName(), originClass));
        } else {
            alertView.setImageResource(R.drawable.ic_alert_inactive);
        }

        if (alert.isDetour()) {
            detourView.setImageResource(R.drawable.ic_detour);
            detourView.setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.ACTIVE_DETOUR_EXPANDED, ScheduleResultsActivity.this, transitType, routeDirectionModel.getRouteId(), routeDirectionModel.getRouteShortName(), originClass));
        } else {
            detourView.setImageResource(R.drawable.ic_detour_inactive);
        }

        if (alert.isSnow()) {
            weatherView.setImageResource(R.drawable.ic_weather);
            weatherView.setOnClickListener(new GoToSystemStatusResultsOnClickListener(Constants.WEATHER_ALERTS_EXPANDED, ScheduleResultsActivity.this, transitType, routeDirectionModel.getRouteId(), routeDirectionModel.getRouteShortName(), originClass));
        } else {
            weatherView.setImageResource(R.drawable.ic_weather_inactive);
        }

        // users cannot subscribe to push notifications from glenside combined
        if (routeDirectionModel.getRouteId().equalsIgnoreCase("GC")) {
            findViewById(R.id.notification_route_switch_label).setVisibility(View.GONE);
            notifsSwitch.setVisibility(View.GONE);

        } else {
            findViewById(R.id.notification_route_switch_label).setVisibility(View.VISIBLE);
            notifsSwitch.setVisibility(View.VISIBLE);

            // set intial checked state of switch
            notifsSwitch.setChecked(SeptaServiceFactory.getNotificationsService().isSubscribedToRoute(ScheduleResultsActivity.this, routeDirectionModel.getRouteId()));

            // switch to create / enable notification for this route
            notifsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                    if (GeneralUtils.isConnectedToInternet(ScheduleResultsActivity.this)) {

                        boolean notifsAllowed = NotificationManagerCompat.from(ScheduleResultsActivity.this).areNotificationsEnabled(),
                                notifsEnabled = SeptaServiceFactory.getNotificationsService().areNotificationsEnabled(ScheduleResultsActivity.this);

                        if (notifsAllowed && notifsEnabled) {
                            if (isChecked) {
                                // enable notifs for route
                                PushNotificationManager.getInstance(ScheduleResultsActivity.this).createNotificationForRoute(routeDirectionModel.getRouteId(), routeDirectionModel.getRouteShortName(), transitType, "Schedule Results");
                            } else {
                                // disable notifs for route
                                PushNotificationManager.getInstance(ScheduleResultsActivity.this).removeNotificationForRoute(routeDirectionModel.getRouteId(), transitType, "Schedule Results");
                            }

                            if (!ignoreSwitch) {
                                PushNotificationManager.updateNotifSubscription(getApplicationContext(), new Runnable() {
                                    @Override
                                    public void run() {
                                        String deviceId = SeptaServiceFactory.getNotificationsService().getDeviceId(getApplicationContext());
                                        CrashlyticsManager.log(Log.ERROR, TAG, "Unable to subscribe device ID: " + deviceId + " to push notifs for route " + routeDirectionModel.getRouteId());

                                        failureToToggleRouteSubscription(isChecked);
                                    }
                                });
                            }
                        } else {
                            // handle lack of permissions
                            showPushNotifsDisabled(notifsAllowed, notifsEnabled);
                            failureToToggleRouteSubscription(isChecked);
                        }
                    } else {
                        // handle no network connection
                        Toast.makeText(ScheduleResultsActivity.this, R.string.subscription_need_connection, Toast.LENGTH_SHORT).show();
                        failureToToggleRouteSubscription(isChecked);
                    }
                }
            });
        }
    }

    private void failureToToggleRouteSubscription(boolean isChecked) {
        ignoreSwitch = true;
        notifsSwitch.setChecked(!isChecked);
        ignoreSwitch = false;
    }

    private void checkIfPushNotifsDisabled() {
        // recheck device permissions and show message if notifs not allowed or enabled
        boolean notifsAllowed = NotificationManagerCompat.from(ScheduleResultsActivity.this).areNotificationsEnabled(),
                notifsEnabled = SeptaServiceFactory.getNotificationsService().areNotificationsEnabled(ScheduleResultsActivity.this);

        if (!notifsAllowed || !notifsEnabled) {
            showPushNotifsDisabled(notifsAllowed, notifsEnabled);
        }
    }

    private void showPushNotifsDisabled(boolean notifsAllowed, boolean notifsEnabled) {
        if (!notifsAllowed) {
            snackbar = Snackbar.make(findViewById(R.id.activity_schedule_results_container), R.string.notifications_permission_needed, Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("Settings", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // link to system notification settings
                    NotificationsManagementFragment.openSystemNotificationSettings(ScheduleResultsActivity.this);
                }
            });
            View snackbarView = snackbar.getView();
            android.widget.TextView tv = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            tv.setMaxLines(10);
            snackbar.show();

        } else if (!notifsEnabled) {
            snackbar = Snackbar.make(findViewById(R.id.activity_schedule_results_container), R.string.notifications_not_enabled, Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("Settings", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // link to notifications management
                    goToNotificationsManagement();
                }
            });
            View snackbarView = snackbar.getView();
            android.widget.TextView tv = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            tv.setMaxLines(10);
            snackbar.show();
        }
    }

    private void goToNotificationsManagement() {
        setResult(Constants.VIEW_NOTIFICATION_MANAGEMENT, new Intent());
        finish();
    }

    class ScheduleResultsAsyncTask extends AsyncTask<Integer, Void, List<ScheduleModel>> {
        ScheduleResultsActivity scheduleResultsActivity;

        ScheduleResultsAsyncTask(ScheduleResultsActivity scheduleResultsActivity) {
            this.scheduleResultsActivity = scheduleResultsActivity;
        }

        @Override
        protected void onPostExecute(List<ScheduleModel> scheduleModels) {
            super.onPostExecute(scheduleModels);
            scheduleResultsActivity.scheduleResultsListView.setAdapter(
                    new ScheduleResultsArrayAdapter(scheduleResultsActivity, scheduleModels));

        }

        @Override
        protected List<ScheduleModel> doInBackground(Integer... params) {
            List<Criteria> criteriaList = new LinkedList<>();
            criteriaList.add(new Criteria("start_stop_id", Criteria.Operation.EQ, scheduleResultsActivity.start.getStopId()));
            criteriaList.add(new Criteria("service_id", Criteria.Operation.EQ, params[0]));
            criteriaList.add(new Criteria("direction_id", Criteria.Operation.EQ, scheduleResultsActivity.routeDirectionModel.getDirectionCode()));
            criteriaList.add(new Criteria("end_stop_id", Criteria.Operation.EQ, scheduleResultsActivity.destination.getStopId()));
            criteriaList.add(new Criteria("route_id", Criteria.Operation.EQ, scheduleResultsActivity.routeDirectionModel.getRouteId()));

            List<ScheduleModel> returnList = new ArrayList<>();
            Cursor cursor = scheduleResultsActivity.scheduleCursorAdapterSupplier.getCursor(scheduleResultsActivity, criteriaList);
            if (cursor.moveToFirst()) {
                do {
                    returnList.add(scheduleCursorAdapterSupplier.getCurrentItemFromCursor(cursor));
                }
                while (cursor.moveToNext());
            }
            return returnList;
        }
    }

    class ScheduleResultsArrayAdapter extends ArrayAdapter<ScheduleModel> {

        public ScheduleResultsArrayAdapter(@NonNull Context context, @NonNull List<ScheduleModel> objects) {
            super(context, 0, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ScheduleModel scheduleModel = getItem(position);
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.custom_row_for_schedules, parent, false);
            }

            DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);

            TextView departureTime = convertView.findViewById(R.id.departure_text);
            departureTime.setText(timeFormat.format(scheduleModel.getDepartureDate()));

            TextView arrivalTime = convertView.findViewById(R.id.arrival_text);
            arrivalTime.setText(timeFormat.format(scheduleModel.getArrivalDate()));

            TextView duration = convertView.findViewById(R.id.duration_text);
            duration.setText(scheduleModel.getDurationAsString());
            duration.setContentDescription(scheduleModel.getDurationAsLongString());

            TextView blockText = convertView.findViewById(R.id.block_text);
            blockText.setHtml(transitType.getString("schedule_trip_prefix", getContext()) + "<b>" + scheduleModel.getBlockId() + "</b>");

            return convertView;
        }
    }

    class ReverseStopAsyncTask extends AsyncTask<Void, Void, Void> {
        ScheduleResultsActivity scheduleResultsActivity;
        boolean found = false;

        ReverseStopAsyncTask(ScheduleResultsActivity scheduleResultsActivity) {
            this.scheduleResultsActivity = scheduleResultsActivity;
        }

        @Override
        protected Void doInBackground(Void... params) {
            StopModel newDest;
            StopModel newStart;
            if (scheduleResultsActivity.transitType != TransitType.RAIL) {
                newDest = getReverse(scheduleResultsActivity.start.getStopId(), scheduleResultsActivity.routeDirectionModel.getRouteShortName());
                newStart = getReverse(scheduleResultsActivity.destination.getStopId(), scheduleResultsActivity.routeDirectionModel.getRouteShortName());

                if (newDest != null && newStart != null) {
                    found = true;
                }
            } else {
                found = true;
                newDest = scheduleResultsActivity.start;
                newStart = scheduleResultsActivity.destination;
            }

            if (found) {
                scheduleResultsActivity.destination = newDest;
                scheduleResultsActivity.start = newStart;
                List<Criteria> criterias = new ArrayList<>(2);
                criterias.add(new Criteria("dircode", Criteria.Operation.EQ, scheduleResultsActivity.routeDirectionModel.getReverseDirectionCode()));
                criterias.add(new Criteria("route_id", Criteria.Operation.EQ, scheduleResultsActivity.routeDirectionModel.getRouteId()));

                Cursor cursor = scheduleResultsActivity.reverseRouteCursorAdapterSupplier.getCursor(scheduleResultsActivity, criterias);
                if (cursor.moveToFirst()) {
                    scheduleResultsActivity.routeDirectionModel = scheduleResultsActivity.reverseRouteCursorAdapterSupplier.getCurrentItemFromCursor(cursor);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (found) {
                scheduleResultsActivity.setUpHeaders();
                int schedule = mapRadioButtonIdtoSchedule(scheduleResultsActivity.radioGroup.getCheckedRadioButtonId(), scheduleResultsActivity.routeDirectionModel.getRouteId());
                ScheduleResultsAsyncTask scheduleResultsAsyncTask = new ScheduleResultsAsyncTask(scheduleResultsActivity);
                scheduleResultsAsyncTask.execute(schedule);
            } else {
                Toast.makeText(ScheduleResultsActivity.this, R.string.reverse_not_found, Toast.LENGTH_LONG).show();
            }
        }

        StopModel getReverse(String stopId, String routeShortName) {
            List<Criteria> criteria = new ArrayList<>(2);
            criteria.add(new Criteria("route_short_name", Criteria.Operation.EQ, routeShortName));
            criteria.add(new Criteria("stop_id", Criteria.Operation.EQ, stopId));
            Cursor cursor = scheduleResultsActivity.reverseStopCursorAdapterSupplier.getCursor(scheduleResultsActivity, criteria);
            if (cursor.moveToFirst()) {
                return scheduleResultsActivity.reverseStopCursorAdapterSupplier.getCurrentItemFromCursor(cursor);
            } else {
                return null;
            }
        }

    }

}
