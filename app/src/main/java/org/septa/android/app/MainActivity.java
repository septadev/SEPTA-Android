package org.septa.android.app;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import org.septa.android.app.about.AboutFragment;
import org.septa.android.app.connect.ConnectFragment;
import org.septa.android.app.database.update.CheckForLatestDB;
import org.septa.android.app.database.update.CleanOldDB;
import org.septa.android.app.database.update.DatabaseSharedPrefsUtils;
import org.septa.android.app.database.update.DatabaseUpgradeUtils;
import org.septa.android.app.database.update.DownloadNewDB;
import org.septa.android.app.database.update.ExpandDBZip;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.fares.FaresFragment;
import org.septa.android.app.fares.PerksFragment;
import org.septa.android.app.favorites.FavoritesFragment;
import org.septa.android.app.favorites.edit.ManageFavoritesFragment;
import org.septa.android.app.nextarrive.NextToArriveFragment;
import org.septa.android.app.notifications.NotificationType;
import org.septa.android.app.notifications.NotificationsManagementFragment;
import org.septa.android.app.notifications.PushNotificationManager;
import org.septa.android.app.schedules.SchedulesFragment;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Alert;
import org.septa.android.app.services.apiinterfaces.model.AlertDetail;
import org.septa.android.app.support.AnalyticsManager;
import org.septa.android.app.support.CrashlyticsManager;
import org.septa.android.app.support.RouteModelComparator;
import org.septa.android.app.support.ShakeDetector;
import org.septa.android.app.systemmap.SystemMapFragment;
import org.septa.android.app.systemstatus.SystemStatusFragment;
import org.septa.android.app.systemstatus.SystemStatusState;
import org.septa.android.app.transitview.TransitViewFragment;
import org.septa.android.app.transitview.TransitViewResultsActivity;
import org.septa.android.app.view.TextView;
import org.septa.android.app.webview.WebViewFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.septa.android.app.connect.ElertsUtils.jumpToElertsApp;
import static org.septa.android.app.database.update.DatabaseSharedPrefsUtils.DEFAULT_DOWNLOAD_REF_ID;
import static org.septa.android.app.transitview.TransitViewFragment.TRANSITVIEW_ROUTE_FIRST;
import static org.septa.android.app.transitview.TransitViewFragment.TRANSITVIEW_ROUTE_SECOND;
import static org.septa.android.app.transitview.TransitViewFragment.TRANSITVIEW_ROUTE_THIRD;

public class MainActivity extends BaseActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        FavoritesFragment.FavoritesFragmentListener,
        ManageFavoritesFragment.ManageFavoritesFragmentListener,
        TransitViewFragment.TransitViewFragmentListener,
        SeptaServiceFactory.SeptaServiceFactoryCallBacks,
        CheckForLatestDB.CheckForLatestDBListener,
        DownloadNewDB.DownloadNewDBListener,
        ExpandDBZip.ExpandDBZipListener,
        CleanOldDB.CleanOldDBListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    NextToArriveFragment nextToArriveFragment = new NextToArriveFragment();
    SchedulesFragment schedules = new SchedulesFragment();

    Fragment activeFragment;
    Drawable previousIcon;
    MenuItem currentMenu;
    NavigationView navigationView;

    // favorites
    FavoritesFragment favoritesFragment;
    ManageFavoritesFragment manageFavoritesFragment;

    SystemStatusFragment systemStatus = new SystemStatusFragment();
    Fragment faresTransitInfo = new FaresFragment();
    Fragment perks = new PerksFragment();
    Fragment systemMap = new SystemMapFragment();
    Fragment trainview = null;
    Fragment notifications = new NotificationsManagementFragment();
    Fragment transitView = new TransitViewFragment();
    Fragment connect = new ConnectFragment();
    Fragment about = new AboutFragment();

    // in-app database update
    DownloadManager downloadManager;
    AlertDialog promptDownloadDB, acknowledgeNewDatabaseReady;

    // shake detector used for crashing the app purposefully -- only in debug and alpha
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    public static final String MOBILE_APP_ALERT_ROUTE_NAME = "Mobile APP",
            MOBILE_APP_ALERT_MODE = "MOBILE",
            GENERIC_ALERT_ROUTE_NAME = "Generic",
            GENERIC_ALERT_MODE = "GENERIC";
    AlertDialog genericAlert, mobileAlert;

    @Override
    public final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        favoritesFragment = new FavoritesFragment();
        trainview = WebViewFragment.getInstance(getResources().getString(R.string.trainview_url));

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // listen for new DB download
        registerReceiver(onDBDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (savedInstanceState == null) {
            if (SeptaServiceFactory.getFavoritesService().getNTAFavorites(this).size() > 0) {
                switchToFavorites();
            } else {
                addNewFavorite();
            }
        }

        // ShakeDetector initialization
        if (BuildConfig.IS_NONPROD_BUILD) {
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mShakeDetector = new ShakeDetector();
            mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
                @Override
                public void onShake(int count) {
                    handleShakeEvent(count);
                }
            });
        }

        // handle new intent
        Intent intent = getIntent();
        int requestCode = intent.getIntExtra(Constants.REQUEST_CODE, -1);
        if (requestCode != -1) {
            onNewIntent(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // re-register the shake detector on resume
        if (BuildConfig.IS_NONPROD_BUILD) {
            mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        }

        // note that generic alert will show up before mobile app alert bc it was the most recently added

        // if mobile app alert(s) exist then pop those up
        if (SystemStatusState.getAlertForApp() != null) {
            final Alert mobileAppAlert = SystemStatusState.getAlertForApp();

            // validate correct alert
            if (MOBILE_APP_ALERT_ROUTE_NAME.equals(mobileAppAlert.getRouteName()) && MOBILE_APP_ALERT_MODE.equals(mobileAppAlert.getMode())) {

                // get alert details
                SeptaServiceFactory.getAlertDetailsService().getAlertDetails(mobileAppAlert.getRouteId()).enqueue(new Callback<AlertDetail>() {
                    @Override
                    public void onResponse(Call<AlertDetail> call, Response<AlertDetail> response) {
                        if (response.body() != null || mobileAppAlert.isAlert()) {
                            AlertDetail alertDetail = response.body();

                            StringBuilder announcement = new StringBuilder();

                            for (AlertDetail.Detail detail : alertDetail.getAlerts()) {
                                announcement.append(detail.getMessage());
                            }

                            // show mobile app alert if current_message not blank
                            if (!announcement.toString().isEmpty()) {
                                showAlert(announcement.toString(), false);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<AlertDetail> call, Throwable t) {
                        SeptaServiceFactory.displayWebServiceError(findViewById(R.id.drawer_layout), MainActivity.this);
                    }
                });
            }
        }

        // if general transit alert(s) exist then pop up global alert(s)
        if (SystemStatusState.getGenericAlert() != null) {
            final Alert genericAlert = SystemStatusState.getGenericAlert();

            if (GENERIC_ALERT_ROUTE_NAME.equals(genericAlert.getRouteName()) && GENERIC_ALERT_MODE.equals(genericAlert.getMode())) {

                // get alert details
                SeptaServiceFactory.getAlertDetailsService().getAlertDetails(genericAlert.getRouteId()).enqueue(new Callback<AlertDetail>() {
                    @Override
                    public void onResponse(Call<AlertDetail> call, Response<AlertDetail> response) {
                        if (response.body() != null || genericAlert.isAlert()) {
                            AlertDetail alertDetail = response.body();

                            StringBuilder announcement = new StringBuilder();

                            for (AlertDetail.Detail detail : alertDetail.getAlerts()) {
                                announcement.append(detail.getMessage());
                            }

                            // show generic alert if current_message not blank
                            if (!announcement.toString().isEmpty()) {
                                showAlert(announcement.toString(), true);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<AlertDetail> call, Throwable t) {
                        SeptaServiceFactory.displayWebServiceError(findViewById(R.id.drawer_layout), MainActivity.this);
                    }
                });
            }
        }

        // check if in-app DB update
        DatabaseUpgradeUtils.checkForNewDatabase(MainActivity.this);

        // prep for new DB if not already installed
        DatabaseUpgradeUtils.prepareForNewDatabase(MainActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // unregister the shake detector on pause
        if (BuildConfig.IS_NONPROD_BUILD) {
            mSensorManager.unregisterListener(mShakeDetector);
        }

        // prevent stacking alertdialogs
        if (genericAlert != null) {
            genericAlert.dismiss();
        }

        if (mobileAlert != null) {
            mobileAlert.dismiss();
        }

        if (promptDownloadDB != null) {
            promptDownloadDB.dismiss();
            promptDownloadDB = null;
        }

        if (acknowledgeNewDatabaseReady != null) {
            acknowledgeNewDatabaseReady.dismiss();
            acknowledgeNewDatabaseReady = null;
        }

        // hide menu badge icon
        View view = navigationView.getMenu().findItem(R.id.nav_system_status).getActionView();
        view.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // remove broadcast listener for DB download completion
        unregisterReceiver(onDBDownloadComplete);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Log.d(TAG, "onNavigationItemSelected Selected:" + item.getTitle());

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        if (id == R.id.nav_next_to_arrive) {
            AnalyticsManager.logContentViewEvent(TAG, AnalyticsManager.CONTENT_VIEW_EVENT_MENU_NEXT_TO_ARRIVE, AnalyticsManager.CONTENT_ID_NEXT_TO_ARRIVE, null);
            switchToBundle(item, nextToArriveFragment, R.string.next_to_arrive, R.drawable.ic_nta_active);
        }

        if (id == R.id.nav_schedule) {
            AnalyticsManager.logContentViewEvent(TAG, AnalyticsManager.CONTENT_VIEW_EVENT_MENU_SCHEDULE, AnalyticsManager.CONTENT_ID_SCHEDULE, null);
            switchToBundle(item, schedules, R.string.schedule, R.drawable.ic_schedule_active);
        }

        if (id == R.id.nav_favorites) {
            AnalyticsManager.logContentViewEvent(TAG, AnalyticsManager.CONTENT_VIEW_EVENT_MENU_FAVORITES, AnalyticsManager.CONTENT_ID_FAVORITES, null);
            switchToBundle(item, favoritesFragment, R.string.favorites, R.drawable.ic_favorites_active);
        }

        if (id == R.id.nav_system_status) {
            AnalyticsManager.logContentViewEvent(TAG, AnalyticsManager.CONTENT_VIEW_EVENT_MENU_SYSTEM_STATUS, AnalyticsManager.CONTENT_ID_SYSTEM_STATUS, null);
            switchToBundle(item, systemStatus, R.string.system_status, R.drawable.ic_status_active);
        }

        if (id == R.id.nav_fares_transit_info) {
            AnalyticsManager.logContentViewEvent(TAG, AnalyticsManager.CONTENT_VIEW_EVENT_MENU_FARES, AnalyticsManager.CONTENT_ID_FARES_TRANSIT, null);
            switchToBundle(item, faresTransitInfo, R.string.fares_and_transit_info, R.drawable.ic_fares_active);
        }

        if (id == R.id.nav_perks) {
            AnalyticsManager.logContentViewEvent(TAG, AnalyticsManager.CONTENT_VIEW_EVENT_MENU_PERKS, AnalyticsManager.CONTENT_ID_PERKS, null);
            switchToBundle(item, perks, R.string.perks, R.drawable.ic_perks_active);
        }

        if (id == R.id.nav_system_map) {
            AnalyticsManager.logContentViewEvent(TAG, AnalyticsManager.CONTENT_VIEW_EVENT_MENU_SYSTEM_MAP, AnalyticsManager.CONTENT_ID_SYSTEM_MAP, null);
            switchToBundle(item, systemMap, R.string.system_map, R.drawable.ic_map_active);
        }

        if (id == R.id.nav_notifications) {
            AnalyticsManager.logContentViewEvent(TAG, AnalyticsManager.CONTENT_VIEW_EVENT_MENU_NOTIFICATIONS, AnalyticsManager.CONTENT_ID_NOTIFICATIONS, null);
            switchToBundle(item, notifications, R.string.notifications, R.drawable.ic_notifications_active);
        }

        if (id == R.id.nav_connect) {
            AnalyticsManager.logContentViewEvent(TAG, AnalyticsManager.CONTENT_VIEW_EVENT_MENU_CONNECT, AnalyticsManager.CONTENT_ID_CONNECT, null);
            switchToBundle(item, connect, R.string.connect_with_septa, 0);
        }

        if (id == R.id.nav_about_app) {
            AnalyticsManager.logContentViewEvent(TAG, AnalyticsManager.CONTENT_VIEW_EVENT_MENU_ABOUT, AnalyticsManager.CONTENT_ID_ABOUT, null);
            switchToBundle(item, about, R.string.about_the_septa_app, 0);
        }

        if (id == R.id.nav_trainview) {
            AnalyticsManager.logContentViewEvent(TAG, AnalyticsManager.CONTENT_VIEW_EVENT_MENU_TRAINVIEW, AnalyticsManager.CONTENT_ID_TRAINVIEW, null);
            switchToBundle(item, trainview, R.string.train_view, 0);
        }

        if (id == R.id.nav_transitview) {
            AnalyticsManager.logContentViewEvent(TAG, AnalyticsManager.CONTENT_VIEW_EVENT_MENU_TRANSITVIEW, AnalyticsManager.CONTENT_ID_TRANSITVIEW, null);
            switchToBundle(item, transitView, R.string.transit_view, R.drawable.ic_transitview_active);
        }

        if (id == R.id.nav_elerts) {
            AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_ELERTS, AnalyticsManager.CUSTOM_EVENT_ID_EXTERNAL_LINK, null);
            jumpToElertsApp(this);
        }
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // ensure intent coming from valid source
        int requestCode = intent.getIntExtra(Constants.REQUEST_CODE, -1);

        switch (requestCode) {
            case Constants.PUSH_NOTIF_REQUEST_RAIL_DELAY:
                PushNotificationManager.onRailDelayNotificationClick(MainActivity.this, intent);
                break;
            case Constants.PUSH_NOTIF_REQUEST_SERVICE_ALERT:
                PushNotificationManager.onSystemStatusNotificationClick(MainActivity.this, intent, Constants.SERVICE_ALERT_EXPANDED, NotificationType.ALERT);
                break;
            case Constants.PUSH_NOTIF_REQUEST_DETOUR:
                PushNotificationManager.onSystemStatusNotificationClick(MainActivity.this, intent, Constants.ACTIVE_DETOUR_EXPANDED, NotificationType.DETOUR);
                break;
        }
    }

    @Override
    public void refreshFavoritesInstance() {
        CrashlyticsManager.log(Log.INFO, TAG, "refreshFavoritesInstance");
        favoritesFragment = new FavoritesFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_content, favoritesFragment).commit();
    }

    @Override
    public void addNewFavorite() {
        AnalyticsManager.logCustomEvent(TAG, AnalyticsManager.CUSTOM_EVENT_ADD_FAVORITE_BUTTON, AnalyticsManager.CUSTOM_EVENT_ID_FAVORITES_MANAGEMENT, null);

        switchToNextToArrive();
    }

    @Override
    public void toggleEditFavoritesMode(boolean isInEditMode) {
        CrashlyticsManager.log(Log.INFO, TAG, "toggling editFavoritesMode");

        if (isInEditMode) {
            // switch to favorites fragment
            favoritesFragment = new FavoritesFragment();
            activeFragment = favoritesFragment;
            getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_content, activeFragment).commit();
        } else {
            // switch to manage favorites fragment
            manageFavoritesFragment = new ManageFavoritesFragment();
            activeFragment = manageFavoritesFragment;
            getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_content, activeFragment).commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int unmaskedRequestCode = requestCode & 0x0000ffff;
        switch (resultCode) {

            case Constants.VIEW_SCHEDULE:

                // jump to schedule from NTA
                if (unmaskedRequestCode == Constants.NTA_REQUEST) {
                    Message message = jumpToSchedulesHandler.obtainMessage();
                    message.setData(data.getExtras());
                    jumpToSchedulesHandler.sendMessage(message);
                }
                break;

            case Constants.VIEW_SYSTEM_STATUS_PICKER:

                // jump to system status from notifications management
                if (unmaskedRequestCode == Constants.NOTIFICATIONS_REQUEST) {
                    jumpToSystemStatusHandler.sendMessage(jumpToSystemStatusHandler.obtainMessage());
                }
                break;

            case Constants.VIEW_NOTIFICATION_MANAGEMENT:

                // jump to notifications management from NTA / schedules / system status / transitview
                if (unmaskedRequestCode == Constants.NTA_REQUEST || unmaskedRequestCode == Constants.SYSTEM_STATUS_REQUEST || unmaskedRequestCode == Constants.SCHEDULES_REQUEST || unmaskedRequestCode == Constants.TRANSITVIEW_REQUEST) {
                    jumpToNotifsManagementHandler.sendMessage(jumpToNotifsManagementHandler.obtainMessage());
                }
                break;
        }
    }

    @Override
    public void gotoSchedules() {
        switchToSchedules(null);
    }

    @Override
    public void goToSchedulesForTarget(StopModel start, StopModel destination, TransitType transitType, RouteDirectionModel routeDirectionModel) {
        // navigate to schedule selection picker
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.STARTING_STATION, start);
        bundle.putSerializable(Constants.DESTINATION_STATION, destination);
        bundle.putSerializable(Constants.TRANSIT_TYPE, transitType);
        if (routeDirectionModel != null) {
            bundle.putSerializable(Constants.ROUTE_DIRECTION_MODEL, routeDirectionModel);
        }

        switchToSchedules(bundle);
    }

    @Override
    public void goToTransitViewResults(RouteDirectionModel firstRoute, RouteDirectionModel secondRoute, RouteDirectionModel thirdRoute) {
        Intent intent = new Intent(this, TransitViewResultsActivity.class);

        // sort the routes and append null routes to the end
        List<RouteDirectionModel> selectedRoutes = new ArrayList<>();
        selectedRoutes.add(firstRoute);
        if (secondRoute != null) {
            selectedRoutes.add(secondRoute);
        }
        if (thirdRoute != null) {
            selectedRoutes.add(thirdRoute);
        }
        Collections.sort(selectedRoutes, new RouteModelComparator());
        while (selectedRoutes.size() < 3) {
            selectedRoutes.add(null);
        }

        intent.putExtra(TRANSITVIEW_ROUTE_FIRST, selectedRoutes.get(0));
        intent.putExtra(TRANSITVIEW_ROUTE_SECOND, selectedRoutes.get(1));
        intent.putExtra(TRANSITVIEW_ROUTE_THIRD, selectedRoutes.get(2));
        startActivityForResult(intent, Constants.TRANSITVIEW_REQUEST);
    }

    @Override
    public void afterLatestDBMetadataLoad(final int latestDBVersion, final String latestDBURL, String updatedDate) {
        boolean shouldPrompt = DatabaseUpgradeUtils.decideWhetherToAskToDownload(MainActivity.this, latestDBVersion, latestDBURL, updatedDate);

        if (shouldPrompt) {
            // prompt user to download new database
            AlertDialog dialog = DatabaseUpgradeUtils.promptToDownload(MainActivity.this);

            // only show prompt once
            if (promptDownloadDB != null && promptDownloadDB.isShowing()) {
                promptDownloadDB.dismiss();
                promptDownloadDB = null;
            }
            promptDownloadDB = dialog;

            // show prompt
            if (promptDownloadDB != null) {
                promptDownloadDB.show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        DatabaseUpgradeUtils.permissionResponseReceived(MainActivity.this, requestCode, permissions, grantResults);
    }

    @Override
    public void afterNewDBDownloadStarted(DownloadManager.Request request, int version) {
        long downloadRefId = downloadManager.enqueue(request);

        DatabaseUpgradeUtils.saveDownloadedVersionNumber(MainActivity.this, downloadRefId, version);
    }

    @Override
    public void clearCorruptedDownloadRefId() {
        long downloadRefId = DatabaseSharedPrefsUtils.getDownloadRefId(MainActivity.this);
        if (downloadRefId != DEFAULT_DOWNLOAD_REF_ID) {
            downloadManager.remove(downloadRefId);
        }
    }

    @Override
    public void afterDBUnzipped(int versionInstalled) {
        // validate that DB unzipped correctly and with correct version, then save if ready
        DatabaseUpgradeUtils.saveNewDatabaseReady(MainActivity.this, versionInstalled);

        notifyNewDatabaseReady();
    }

    @Override
    public void notifyNewDatabaseReady() {
        // only show that new database ready if not already using most up to date version of database
        AlertDialog dialog = DatabaseUpgradeUtils.buildNewDatabaseReadyPopUp(MainActivity.this);

        // only show prompt once
        if (acknowledgeNewDatabaseReady != null && acknowledgeNewDatabaseReady.isShowing()) {
            acknowledgeNewDatabaseReady.dismiss();
            acknowledgeNewDatabaseReady = null;
        }
        acknowledgeNewDatabaseReady = dialog;

        // show prompt
        if (acknowledgeNewDatabaseReady != null) {
            acknowledgeNewDatabaseReady.show();
        }
    }

    @Override
    public void afterOldDBCleaned() {
        DatabaseUpgradeUtils.databaseUpdateComplete(MainActivity.this);
    }

    private void switchToBundle(MenuItem item, Fragment targetFragment, int title, int highlightedIcon) {
        CrashlyticsManager.log(Log.INFO, TAG, "switchToBundle:" + item.getTitle() + ", " + targetFragment.getClass().getCanonicalName());
        if ((currentMenu != null) && item.getItemId() == currentMenu.getItemId()) {
            return;
        }

        if (previousIcon != null) {
            currentMenu.setIcon(previousIcon);
        }
        currentMenu = item;
        previousIcon = item.getIcon();
        if (highlightedIcon != 0) {
            currentMenu.setIcon(highlightedIcon);
        }
        activeFragment = targetFragment;

        getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_content, targetFragment).commit();

        setTitle(title);
    }

    public void switchToNextToArrive() {
        if (currentMenu == null || currentMenu.getItemId() != R.id.nav_next_to_arrive) {
            if (currentMenu != null) {
                currentMenu.setIcon(previousIcon);
            }
            navigationView.setCheckedItem(R.id.nav_next_to_arrive);
            currentMenu = navigationView.getMenu().findItem(R.id.nav_next_to_arrive);
            previousIcon = currentMenu.getIcon();
            currentMenu.setIcon(R.drawable.ic_nta_active);
            getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_content, nextToArriveFragment).commit();
            setTitle(R.string.next_to_arrive);
        }
    }

    public void switchToFavorites() {
        CrashlyticsManager.log(Log.INFO, TAG, "switchToFavorites");
        if (currentMenu == null || currentMenu.getItemId() != R.id.nav_favorites) {
            if (currentMenu != null) {
                currentMenu.setIcon(previousIcon);
            }
            navigationView.setCheckedItem(R.id.nav_favorites);
            currentMenu = navigationView.getMenu().findItem(R.id.nav_favorites);
            previousIcon = currentMenu.getIcon();
            currentMenu.setIcon(R.drawable.ic_favorites_active);
            getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_content, favoritesFragment).commit();
            setTitle(R.string.favorites);
        }
    }

    public void switchToSystemStatus() {
        if (currentMenu == null || currentMenu.getItemId() != R.id.nav_system_status) {
            if (currentMenu != null) {
                currentMenu.setIcon(previousIcon);
            }
            navigationView.setCheckedItem(R.id.nav_system_status);
            currentMenu = navigationView.getMenu().findItem(R.id.nav_system_status);
            previousIcon = currentMenu.getIcon();
            currentMenu.setIcon(R.drawable.ic_status_active);
            getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_content, systemStatus).commit();
            setTitle(R.string.system_status);
        }
    }

    public void switchToNotificationManagement() {
        if (currentMenu == null || currentMenu.getItemId() != R.id.nav_notifications) {
            if (currentMenu != null) {
                currentMenu.setIcon(previousIcon);
            }
            navigationView.setCheckedItem(R.id.nav_notifications);
            currentMenu = navigationView.getMenu().findItem(R.id.nav_notifications);
            previousIcon = currentMenu.getIcon();
            currentMenu.setIcon(R.drawable.ic_notifications_active);
            getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_content, notifications).commit();
            setTitle(R.string.notifications);
        }
    }

    public void switchToSchedules(Bundle data) {
        CrashlyticsManager.log(Log.INFO, TAG, "switchToSchedules");

        if (currentMenu == null || currentMenu.getItemId() != R.id.nav_schedule) {
            if (currentMenu != null) {
                currentMenu.setIcon(previousIcon);
            }
            navigationView.setCheckedItem(R.id.nav_schedule);
            currentMenu = navigationView.getMenu().findItem(R.id.nav_schedule);
            previousIcon = currentMenu.getIcon();
            currentMenu.setIcon(R.drawable.ic_schedule_active);
            schedules = SchedulesFragment.newInstance();

            try {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_content, schedules).commit();
            } catch (Exception e) {
                CrashlyticsManager.logException(TAG, e);
                return;
            }

            if (data != null) {
                schedules.prePopulate(data);
            }
            setTitle(R.string.schedule);
        }
    }

    private Handler jumpToSchedulesHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switchToSchedules(msg.getData());
        }
    };

    private Handler jumpToSystemStatusHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switchToSystemStatus();
        }
    };

    private Handler jumpToNotifsManagementHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switchToNotificationManagement();
        }
    };

    // listener for completed database downloads
    BroadcastReceiver onDBDownloadComplete = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            // get the refid from the download manager
            long referenceIdFound = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            // grab refid we are looking for from shared pref
            long referenceIdLookingFor = DatabaseSharedPrefsUtils.getDownloadRefId(MainActivity.this);

            Log.d(TAG, "Found DB download ref ID " + referenceIdFound + " and looking for " + referenceIdLookingFor);

            if (referenceIdFound == referenceIdLookingFor && referenceIdLookingFor != DEFAULT_DOWNLOAD_REF_ID) {
                // stop looking for that download id
                DatabaseSharedPrefsUtils.clearDownloadRefId(MainActivity.this);

                // expand new db
                DatabaseUpgradeUtils.prepareForNewDatabase(MainActivity.this);

                Log.d(TAG, "Completed download for ref ID: " + referenceIdFound);
            }
        }
    };

    public void showAlert(String alert, Boolean isGenericAlert) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (isGenericAlert) {
            builder.setTitle(R.string.title_generic_alert);
        } else {
            builder.setTitle(R.string.title_mobile_app_alert);
        }

        // make message HTML enabled and allow for anchor links
        View alertView = getLayoutInflater().inflate(R.layout.dialog_alert, null);
        TextView message = alertView.findViewById(R.id.dialog_alert_message);
        final SpannableString s = new SpannableString(alert);
        message.setText(Html.fromHtml(s.toString()));
        message.setMovementMethod(LinkMovementMethod.getInstance());
        Linkify.addLinks(message, Linkify.WEB_URLS);

        builder.setView(alertView);

        builder.setNeutralButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();

        if (isGenericAlert) {
            genericAlert = dialog;
        } else {
            mobileAlert = dialog;
        }

        // show badge icon in menu here
        View view = navigationView.getMenu().findItem(R.id.nav_system_status).getActionView();
        view.setVisibility(View.VISIBLE);

        dialog.show();
    }

    public void handleShakeEvent(int count) {
        Log.d(TAG, "Device shaken!");

        // force crash the app
        throw new RuntimeException("This is a forced crash");
    }

}
