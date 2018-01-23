package org.septa.android.app;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import org.septa.android.app.about.AboutFragment;
import org.septa.android.app.connect.ConnectFragment;
import org.septa.android.app.fares.FaresFragment;
import org.septa.android.app.favorites.FavoritesFragment;
import org.septa.android.app.favorites.FavoritesFragmentCallBacks;
import org.septa.android.app.nextarrive.NextToArriveFragment;
import org.septa.android.app.schedules.SchedulesFragment;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.support.AnalyticsManager;
import org.septa.android.app.support.CrashlyticsManager;
import org.septa.android.app.systemmap.SystemMapFragment;
import org.septa.android.app.systemstatus.SystemStatusFragment;
import org.septa.android.app.webview.WebViewFragment;

/**
 * Created by jkampf on 8/22/17.
 */

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FavoritesFragmentCallBacks {

    public static final String TAG = MainActivity.class.getSimpleName();
    NextToArriveFragment nextToArriveFragment = new NextToArriveFragment();
    SchedulesFragment schedules = new SchedulesFragment();

    Fragment activeFragment;
    Drawable previousIcon;
    MenuItem currentMenu;
    NavigationView navigationView;

    FavoritesFragment favorites;

    SystemStatusFragment systemStatus = new SystemStatusFragment();
    Fragment faresTransitInfo = new FaresFragment();
    Fragment systemMap = new SystemMapFragment();
    Fragment events = null;
    Fragment trainview = null;
    Fragment transitview = null;
    Fragment connect = new ConnectFragment();
    Fragment about = new AboutFragment();

    @Override
    public final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        favorites = FavoritesFragment.newInstance();
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

        if (savedInstanceState == null)
            if (SeptaServiceFactory.getFavoritesService().getFavorites(this).size() > 0) {
                switchToFavorites();
            } else {
                addNewFavorite();
            }

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
            switchToBundle(item, favorites, R.string.favorites, R.drawable.ic_favorites_active);
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

    private void switchToBundle(MenuItem item, Fragment targetFragment, int title, int highlitghtedIcon) {
        CrashlyticsManager.log(Log.INFO, TAG, "switchToBundle:" + item.getTitle() + ", " + targetFragment.getClass().getCanonicalName());
        if ((currentMenu != null) && item.getItemId() == currentMenu.getItemId())
            return;

        if (previousIcon != null) {
            currentMenu.setIcon(previousIcon);
        }
        currentMenu = item;
        previousIcon = item.getIcon();
        if (highlitghtedIcon != 0)
            currentMenu.setIcon(highlitghtedIcon);
        activeFragment = targetFragment;

        getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_content, targetFragment).commit();

        setTitle(title);
    }

    @Override
    public void addNewFavorite() {
        CrashlyticsManager.log(Log.INFO, TAG, "addNewFavorite");
        if (currentMenu == null || currentMenu.getItemId() != R.id.nav_next_to_arrive) {
            if (currentMenu != null)
                currentMenu.setIcon(previousIcon);
            navigationView.setCheckedItem(R.id.nav_next_to_arrive);
            currentMenu = navigationView.getMenu().findItem(R.id.nav_next_to_arrive);
            previousIcon = currentMenu.getIcon();
            currentMenu.setIcon(R.drawable.ic_nta_active);
            getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_content, nextToArriveFragment).commit();
            setTitle(R.string.next_to_arrive);
        }
    }

    @Override
    public void gotoSchedules() {
        switchToSchedules(null);
        //switchToBundle(navigationView.getMenu().findItem(R.id.nav_schedule), schedules, R.string.schedule, R.drawable.ic_schedule_active);
    }

    public void switchToFavorites() {
        CrashlyticsManager.log(Log.INFO, TAG, "switchToFavorites");
        if (currentMenu == null || currentMenu.getItemId() != R.id.nav_favorites) {
            if (currentMenu != null)
                currentMenu.setIcon(previousIcon);
            navigationView.setCheckedItem(R.id.nav_favorites);
            currentMenu = navigationView.getMenu().findItem(R.id.nav_favorites);
            previousIcon = currentMenu.getIcon();
            currentMenu.setIcon(R.drawable.ic_favorites_active);
            getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_content, favorites).commit();
            setTitle(R.string.favorites);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int unmaskedRequestCode = requestCode & 0x0000ffff;
        if (unmaskedRequestCode == Constants.NTA_REQUEST) {
            if (resultCode == Constants.VIEW_SCHEDULE) {
                Message message = jumpToScheduelsHandler.obtainMessage();
                message.setData(data.getExtras());
                jumpToScheduelsHandler.sendMessage(message);
            }
        }

    }

    private Handler jumpToScheduelsHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switchToSchedules(msg.getData());
        }
    };


    public void switchToSchedules(Bundle data) {
        CrashlyticsManager.log(Log.INFO, TAG, "switchToSchedules");

        if (currentMenu == null || currentMenu.getItemId() != R.id.nav_schedule) {
            if (currentMenu != null)
                currentMenu.setIcon(previousIcon);
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

    @Override
    public void refresh() {
        CrashlyticsManager.log(Log.INFO, TAG, "refresh");
        favorites = FavoritesFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_content, favorites).commit();

    }


}
