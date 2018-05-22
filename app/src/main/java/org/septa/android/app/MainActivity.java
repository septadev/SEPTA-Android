package org.septa.android.app;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
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
import org.septa.android.app.database.CheckForLatestDB;
import org.septa.android.app.database.DownloadNewDB;
import org.septa.android.app.database.SEPTADatabase;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.fares.FaresFragment;
import org.septa.android.app.favorites.FavoritesFragment;
import org.septa.android.app.favorites.edit.ManageFavoritesFragment;
import org.septa.android.app.nextarrive.NextToArriveFragment;
import org.septa.android.app.schedules.SchedulesFragment;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Alert;
import org.septa.android.app.services.apiinterfaces.model.AlertDetail;
import org.septa.android.app.services.apiinterfaces.model.Favorite;
import org.septa.android.app.support.AnalyticsManager;
import org.septa.android.app.support.CrashlyticsManager;
import org.septa.android.app.systemmap.SystemMapFragment;
import org.septa.android.app.systemstatus.SystemStatusFragment;
import org.septa.android.app.systemstatus.SystemStatusState;
import org.septa.android.app.view.TextView;
import org.septa.android.app.webview.WebViewFragment;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by jkampf on 8/22/17.
 */

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FavoritesFragment.FavoritesFragmentListener, ManageFavoritesFragment.ManageFavoritesFragmentListener, SeptaServiceFactory.SeptaServiceFactoryCallBacks, CheckForLatestDB.CheckForLatestDBListener, DownloadNewDB.DownloadNewDBListener {

    private static final String SHARED_PREFERENCES_DATABASE = "SHARED_PREFERENCES_DATABASE";

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
    Fragment systemMap = new SystemMapFragment();
    Fragment events = null;
    Fragment trainview = null;
    Fragment transitview = null;
    Fragment connect = new ConnectFragment();
    Fragment about = new AboutFragment();

    // in-app database update
    CheckForLatestDB checkForLatestDB;
    DownloadNewDB downloadNewDB;
    DownloadManager downloadManager;
    List<Long> downloadRefIds;
    private static final String PERMISSION_TO_DOWNLOAD = "PERMISSION_TO_DOWNLOAD";

    public static final String MOBILE_APP_ALERT_ROUTE_NAME = "Mobile APP",
            MOBILE_APP_ALERT_MODE = "MOBILE",
            GENERIC_ALERT_ROUTE_NAME = "Generic",
            GENERIC_ALERT_MODE = "GENERIC";
    AlertDialog genericAlert, mobileAlert;

    @Override
    public final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        favoritesFragment = FavoritesFragment.newInstance();
        events = WebViewFragment.getInstance(getResources().getString(R.string.events_url));
        trainview = WebViewFragment.getInstance(getResources().getString(R.string.trainview_url));
        transitview = WebViewFragment.getInstance(getResources().getString(R.string.transitview_url));

        setContentView(R.layout.main_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // listen for new DB download
        registerReceiver(onDBDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        downloadRefIds = new ArrayList<>();

        if (savedInstanceState == null) {
            if (SeptaServiceFactory.getFavoritesService().getFavorites(this).size() > 0) {
                switchToFavorites();
            } else {
                addNewFavorite();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

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
        if (isConnectedToInternet()) {
            checkForLatestDB = new CheckForLatestDB(MainActivity.this);
            checkForLatestDB.execute();
        } else {
            // do nothing -- do not tell user there may be a new DB available
            Log.d(TAG, "No network connection established -- cannot check for new DB");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // prevent stacking alertdialogs
        if (genericAlert != null) {
            genericAlert.dismiss();
        }

        if (mobileAlert != null) {
            mobileAlert.dismiss();
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        if (id == R.id.nav_next_to_arrive) {
            AnalyticsManager.logContentType(TAG, AnalyticsManager.CUSTOM_EVENT_NEXT_TO_ARRIVE, null, null);
            switchToBundle(item, nextToArriveFragment, R.string.next_to_arrive, R.drawable.ic_nta_active);
        }

        if (id == R.id.nav_schedule) {
            AnalyticsManager.logContentType(TAG, AnalyticsManager.CUSTOM_EVENT_SCHEDULE, null, null);
            switchToBundle(item, schedules, R.string.schedule, R.drawable.ic_schedule_active);
        }

        if (id == R.id.nav_favorites) {
            AnalyticsManager.logContentType(TAG, AnalyticsManager.CUSTOM_EVENT_FAVORITES, null, null);
            switchToBundle(item, favoritesFragment, R.string.favorites, R.drawable.ic_favorites_active);
        }

        if (id == R.id.nav_system_status) {
            AnalyticsManager.logContentType(TAG, AnalyticsManager.CUSTOM_EVENT_SYSTEM_STATUS, null, null);
            switchToBundle(item, systemStatus, R.string.system_status, R.drawable.ic_status_active);
        }

        if (id == R.id.nav_fares_transit_info) {
            AnalyticsManager.logContentType(TAG, AnalyticsManager.CUSTOM_EVENT_FARES_TRANSIT, null, null);
            switchToBundle(item, faresTransitInfo, R.string.fares_and_transit_info, R.drawable.ic_fares_active);
        }

        if (id == R.id.nav_system_map) {
            AnalyticsManager.logContentType(TAG, AnalyticsManager.CUSTOM_EVENT_SYSTEM_MAP, null, null);
            switchToBundle(item, systemMap, R.string.system_map, R.drawable.ic_map_active);
        }

        if (id == R.id.nav_events) {
            AnalyticsManager.logContentType(TAG, AnalyticsManager.CUSTOM_EVENT_SPECIAL_EVENTS, null, null);
            switchToBundle(item, events, R.string.events, R.drawable.ic_calendar_active);
        }

        if (id == R.id.nav_connect) {
            AnalyticsManager.logContentType(TAG, AnalyticsManager.CUSTOM_EVENT_CONNECT, null, null);
            switchToBundle(item, connect, R.string.connect_with_septa, 0);
        }

        if (id == R.id.nav_about_app) {
            AnalyticsManager.logContentType(TAG, AnalyticsManager.CUSTOM_EVENT_ABOUT, null, null);
            switchToBundle(item, about, R.string.about_the_septa_app, 0);
        }

        if (id == R.id.nav_trainview) {
            AnalyticsManager.logContentType(TAG, AnalyticsManager.CUSTOM_EVENT_TRAIN_VIEW, null, null);
            switchToBundle(item, trainview, R.string.train_view, 0);
        }

        if (id == R.id.nav_transitview) {
            AnalyticsManager.logContentType(TAG, AnalyticsManager.CUSTOM_EVENT_TRANSIT_VIEW, null, null);
            switchToBundle(item, transitview, R.string.transit_view, 0);
        }
        return true;
    }

    @Override
    public void refreshFavoritesInstance() {
        CrashlyticsManager.log(Log.INFO, TAG, "refreshFavoritesInstance");
        favoritesFragment = FavoritesFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_content, favoritesFragment).commit();
    }

    @Override
    public void addNewFavorite() {
        CrashlyticsManager.log(Log.INFO, TAG, "addNewFavorite");
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

    @Override
    public void toggleEditFavoritesMode(boolean isInEditMode) {
        CrashlyticsManager.log(Log.INFO, TAG, "toggling editFavoritesMode");

        if (isInEditMode) {
            // switch to favorites fragment
            favoritesFragment = FavoritesFragment.newInstance();
            activeFragment = favoritesFragment;
            getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_content, activeFragment).commit();
        } else {
            // open edit mode
            List<Favorite> favoriteList = favoritesFragment.openEditMode();

            // switch to manage favorites fragment
            manageFavoritesFragment = ManageFavoritesFragment.newInstance(favoriteList);
            activeFragment = manageFavoritesFragment;
            getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_content, activeFragment).commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int unmaskedRequestCode = requestCode & 0x0000ffff;
        if (unmaskedRequestCode == Constants.NTA_REQUEST) {
            if (resultCode == Constants.VIEW_SCHEDULE) {
                Message message = jumpToSchedulesHandler.obtainMessage();
                message.setData(data.getExtras());
                jumpToSchedulesHandler.sendMessage(message);
            }
        }
    }

    @Override
    public void gotoSchedules() {
        switchToSchedules(null);
        //switchToBundle(navigationView.getMenu().findItem(R.id.nav_schedule), schedules, R.string.schedule, R.drawable.ic_schedule_active);
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

    public boolean isConnectedToInternet() {
        ConnectivityManager connectivity = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (NetworkInfo anInfo : info) {
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void switchToBundle(MenuItem item, Fragment targetFragment, int title, int highlightedIcon) {
        CrashlyticsManager.log(Log.INFO, TAG, "switchToBundle:" + item.getTitle() + ", " + targetFragment.getClass().getCanonicalName());
        if ((currentMenu != null) && item.getItemId() == currentMenu.getItemId())
            return;

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

    private void switchToNextToArrive() {
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

    @Override
    public void afterLatestDBMetadataLoad(int latestDBVersion, final String latestDBURL, String updatedDate) {
        // check API for DB version number
        int currentDBVersion = SEPTADatabase.getDatabaseVersion();

        // TODO: remove
        Log.e(TAG, "Latest DB Version: " + latestDBVersion + " vs. Old DB Version: " + currentDBVersion);

        // check if newer database available
        // TODO: switch this later
        if (latestDBVersion == 14) {
//        if (latestDBVersion > currentDBVersion) {

            // check if permission granted previously
            if (!getSharedPreferences(SHARED_PREFERENCES_DATABASE, Context.MODE_PRIVATE).getBoolean(PERMISSION_TO_DOWNLOAD, false)) {

                // prompt user to download new database
                final AlertDialog dialog = new AlertDialog.Builder(this).setCancelable(true).setTitle(R.string.prompt_download_database_title)
                        .setMessage(R.string.prompt_download_database_description)

                        // approved download
                        .setPositiveButton(R.string.prompt_download_button_positive_download_now, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // download new DB zip from url given by API
                                if (latestDBURL != null && !latestDBURL.isEmpty()) {
                                    // TODO: save permission to download - uncomment later
//                                    getSharedPreferences(SHARED_PREFERENCES_DATABASE, Context.MODE_PRIVATE).edit().putBoolean(PERMISSION_TO_DOWNLOAD, true).apply();

                                    // do not need to recheck for connection -- handled by DownloadManager
                                    downloadNewDB = new DownloadNewDB(MainActivity.this, latestDBURL);
                                    downloadNewDB.execute();
                                }
                            }
                        })

                        // remind of download later
                        .setNegativeButton(R.string.prompt_download_button_negative_remind_later, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })

                        // link to NTA
                        .setNeutralButton(R.string.prompt_download_button_neutral_nta, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                switchToNextToArrive();
                            }
                        })
                        .create();

                // set "download now" button color
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
                    }
                });

                dialog.show();
            }
        }
    }

    @Override
    public void afterNewDBDownload(DownloadManager.Request request) {
        // add to list of downloads
        downloadRefIds.add(downloadManager.enqueue(request));

        // TODO: how to handle interrupted downloads? -- DownloadManager should handle it all...
    }

    // listener for completed database downloads
    BroadcastReceiver onDBDownloadComplete = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            // get the refid from the download manager
            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            // remove it from our list
            downloadRefIds.remove(referenceId);

            // if list is empty means all downloads completed
            if (downloadRefIds.isEmpty()) {

                prepareForNewDatabase();

                // show notification that download completed
                Log.e(TAG, "Completed download for ref ID: " + referenceId);
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.notification_database_download_complete));   // TODO: language for complete download notification
                // TODO: set action -- what does clicking on notif do?
                // TODO: clicking on notif with app open --> restart app?
                // TODO: clicking on notif just opens app

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.notify(455, mBuilder.build());
                }

                // prompt user to restart app?
                final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).setCancelable(true).setTitle(R.string.prompt_restart_database_title)
                        .setMessage(R.string.prompt_restart_database_description)

                        // approved restart
                        .setPositiveButton(R.string.prompt_restart_button_positive_now, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 0.5 sec delay to restart
                                int delay = 500;

                                // restart app
                                restartApplication(delay);
                            }
                        })

                        // remind of download later
                        .setNegativeButton(R.string.prompt_restart_button_negative_later, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();

                // set "restart now" button color
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
                    }
                });

                dialog.show();

            }

        }
    };

    private void prepareForNewDatabase() {
        // TODO: expand new DB zip

        // TODO: run script to create indices on new DB

        // TODO: set new DB -- rename new DB with old name? does this overwrite / delete old DB

//                    SEPTADatabase.setDatabaseVersion(newDBVersion);

        // TODO: delete downloaded zip

        // TODO: delete old version of DB

        // restore download permission back to false
        getSharedPreferences(SHARED_PREFERENCES_DATABASE, Context.MODE_PRIVATE).edit().putBoolean(PERMISSION_TO_DOWNLOAD, false).apply();

    }

    private void restartApplication(int delay) {
        // restart the app
        Intent restartIntent = MainActivity.this.getPackageManager().getLaunchIntentForPackage(MainActivity.this.getPackageName());
        PendingIntent intent = PendingIntent.getActivity(MainActivity.this, 0, restartIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager manager = (AlarmManager) MainActivity.this.getSystemService(Context.ALARM_SERVICE);
        manager.set(AlarmManager.RTC, System.currentTimeMillis() + delay, intent);
        System.exit(2);
    }

    public void showAlert(String alert, Boolean isGenericAlert) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (isGenericAlert) builder.setTitle(R.string.title_generic_alert);
        else builder.setTitle(R.string.title_mobile_app_alert);

        // make message HTML enabled and allow for anchor links
        View alertView = getLayoutInflater().inflate(R.layout.dialog_alert, null);
        TextView message = (TextView) alertView.findViewById(R.id.dialog_alert_message);
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

}
