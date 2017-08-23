package org.septa.android.app;

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
    Integer currentBundleKey;
    Fragment activeFragement;

    @Override
    public final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
            switchToBundle(R.id.nav_next_to_arrive, nextToArriveFragment, R.string.next_to_arrive);
        }

        if (id == R.id.nav_schedule) {
            switchToBundle(R.id.nav_schedule, schedules, R.string.schedule);
        }

        return true;
    }

    private void switchToBundle(int navItem, Fragment targetFragment, int title) {
        if ((currentBundleKey != null) && navItem == currentBundleKey)
            return;

        // TODO Need to implement saving the state of the fragements.
        if (activeFragement != null) {
            Bundle bundle = new Bundle();
            activeFragement.onSaveInstanceState(bundle);
            fragmentStateMap.put(currentBundleKey, bundle);
        }

        currentBundleKey = navItem;
        Bundle targetBundle = fragmentStateMap.get(currentBundleKey);
        activeFragement = targetFragment;
        if (targetBundle != null)
            targetFragment.setArguments(targetBundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_content, targetFragment).commit();

        setTitle(title);
    }

}
