package org.septa.android.app.nextarrive;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.septa.android.app.FirstLevelBaseActivity;
import org.septa.android.app.R;
import org.septa.android.app.database.DatabaseManager;
import org.septa.android.app.support.TabActivityHandler;


public class NextToArrive extends FirstLevelBaseActivity {

    public static final String TAG = NextToArrive.class.getSimpleName();

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    TabActivityHandler tabActivityHandlers[];

    @Override
    protected void activityOnCreate(Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);


        DatabaseManager dbManager = DatabaseManager.getInstance(this);

        tabActivityHandlers = new TabActivityHandler[5];
        tabActivityHandlers[1] = new RailTabActivityHandler(getString(R.string.rail_tab), dbManager.getRailStopCursorAdapterSupplier(), R.drawable.rail_white);
        tabActivityHandlers[0] = new BusTabActivityHandler(getString(R.string.bus_tab), dbManager.getBusRouteCursorAdapterSupplier(), dbManager.getBusStopCursorAdapterSupplier(), dbManager.getBusStopAfterCursorAdapterSupplier(), R.drawable.bus_white);
        tabActivityHandlers[3] = new BusTabActivityHandler(getString(R.string.trolly_tab), dbManager.getTrollyRouteCursorAdapterSupplier(), dbManager.getTrollyStopCursorAdapterSupplier(), dbManager.getTrollyStopAfterCursorAdapterSupplier(), R.drawable.trolley_white);
        tabActivityHandlers[2] = new BusTabActivityHandler(getString(R.string.subway_tab), dbManager.getSubwayRouteCursorAdapterSupplier(), dbManager.getSubwayStopCursorAdapterSupplier(), dbManager.getSubwayStopAfterCursorAdapterSupplier(), R.drawable.subway_white);
        tabActivityHandlers[4] = new RailTabActivityHandler(getString(R.string.nhsl_tab), dbManager.getNhslStopCursorAdapterSupplier(), R.drawable.nhsl_white);

        setContentView(R.layout.activity_next_to_arrive);


        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        setUpTabs(tabLayout);
    }

    private void setUpTabs(TabLayout tabLayout) {
        for (int i = 0; i < tabActivityHandlers.length; i++) {
            TextView tab = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
            tab.setText(tabActivityHandlers[i].getTabTitle());
            tab.setCompoundDrawablesWithIntrinsicBounds(tabActivityHandlers[i].getDrawableId(), 0, 0, 0);
            tabLayout.getTabAt(i).setCustomView(tab);
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return tabActivityHandlers[position].getFragment();
        }

        @Override
        public int getCount() {
            return tabActivityHandlers.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabActivityHandlers[position].getTabTitle();
        }
    }

}
