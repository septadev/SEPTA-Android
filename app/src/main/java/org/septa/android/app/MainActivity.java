package org.septa.android.app;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.IntegerRes;
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

import org.septa.android.app.nextarrive.NextToArriveFragement;
import org.septa.android.app.schedules.SchedulesFragment;
import org.septa.android.app.temp.ComingSoonFragement;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jkampf on 8/22/17.
 */

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    NextToArriveFragement nextToArriveFragment = new NextToArriveFragement();
    SchedulesFragment schedules = new SchedulesFragment();

    Map<Integer, Bundle> fragmentStateMap = new HashMap<Integer, Bundle>();
    Fragment activeFragement;
    Drawable previousIcon;
    MenuItem currentMenu;

    Fragment favorites = new ComingSoonFragement();
    Fragment systemStatus = new ComingSoonFragement();
    Fragment faresTransitInfo = new ComingSoonFragement();
    Fragment subwayMap = new ComingSoonFragement();
    Fragment events = new ComingSoonFragement();
    Fragment connect = new ComingSoonFragement();
    Fragment about = new ComingSoonFragement();

    @Override
    public final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        // getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_content, nextToArriveFragment).commit();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Log.d(TAG, "onOptionsItemSelected Selected:" + item.getTitle());

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        // }

        return super.onOptionsItemSelected(item);
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
            switchToBundle(item, nextToArriveFragment, R.string.next_to_arrive, R.drawable.ic_next_to_arrive_fill);
        }

        if (id == R.id.nav_schedule) {
            switchToBundle(item, schedules, R.string.schedule, R.drawable.ic_schedule_fill);
        }

        if (id == R.id.nav_favorites) {
            switchToBundle(item, favorites, R.string.favorites, R.drawable.ic_heart_fill);
        }

        if (id == R.id.nav_system_status) {
            switchToBundle(item, systemStatus, R.string.system_status, R.drawable.ic_status_fill);

        }

        if (id == R.id.nav_fares_transit_info) {
            switchToBundle(item, faresTransitInfo, R.string.fares_and_transit_info, R.drawable.ic_fairs_fill);
        }

        if (id == R.id.nav_subway_map) {
            switchToBundle(item, subwayMap, R.string.subway_map, R.drawable.ic_subway_map_fill);
        }

        if (id == R.id.nav_events) {
            switchToBundle(item, events, R.string.events, R.drawable.ic_events_fill);
        }

        if (id == R.id.nav_connect) {
            switchToBundle(item, connect, R.string.connect_with_septa, 0);
        }

        if (id == R.id.nav_about_app) {
            switchToBundle(item, about, R.string.about_the_septa_app, 0);
        }

        return true;
    }

    private void switchToBundle(MenuItem item, Fragment targetFragment, int title, int highlitghtedIcon) {
        if ((currentMenu != null) && item.getItemId() == currentMenu.getItemId())
            return;

        // TODO Need to implement saving the state of the fragements.
        if (activeFragement != null) {
            Bundle bundle = new Bundle();
            activeFragement.onSaveInstanceState(bundle);
            fragmentStateMap.put(item.getItemId(), bundle);
        }

        if (previousIcon != null) {
            currentMenu.setIcon(previousIcon);
        }
        currentMenu = item;
        previousIcon = item.getIcon();
        if (highlitghtedIcon != 0)
            currentMenu.setIcon(highlitghtedIcon);
        Bundle targetBundle = fragmentStateMap.get(item.getItemId());
        activeFragement = targetFragment;
        if (targetBundle != null)
            targetFragment.setArguments(targetBundle);


        getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_content, targetFragment).commit();

        setTitle(title);
    }

}
