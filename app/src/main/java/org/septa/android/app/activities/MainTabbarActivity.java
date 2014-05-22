/*
 * MainTabbarActivity.java
 * Last modified on 01-29-2014 14:46-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.fragments.ConnectListFragment;
import org.septa.android.app.fragments.RealtimeMenuFragment;
import org.septa.android.app.fragments.SchedulesListFragment;
import org.septa.android.app.fragments.TransitMapImageViewFragment;
import org.septa.android.app.models.ObjectFactory;

public class MainTabbarActivity extends BaseAnalyticsActionBarActivity implements ActionBar.TabListener {
    public static final String TAG = MainTabbarActivity.class.getName();

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link android.support.v4.view.ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabbar_activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        View customActionBarTitleView = getLayoutInflater().inflate(R.layout.actionbar_customtitle, null);
        TextView appNameTextView = (TextView)customActionBarTitleView.findViewById(R.id.actionbar_customtitle_appname);
        appNameTextView.setText(getResources().getString(R.string.titlebar_prefix_text));

        actionBar.setCustomView(customActionBarTitleView);

        // Create the adapter that will return a fragment for each of the four
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        for (String tab : getResources().getStringArray(R.array.nav_main_items)) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(tab)
                            .setTabListener(this)
            );
        }

        int maintabbarSelectedSection = ObjectFactory.getInstance()
                .getSharedPreferencesManager(this)
                .getMainTabbarSelectedSection();

        actionBar.setSelectedNavigationItem(maintabbarSelectedSection);
    }

    @Override
    protected void onStop() {
        ObjectFactory.getInstance().getSharedPreferencesManager(this)
                .setMainTabbarSelectedSection(getSupportActionBar()
                .getSelectedNavigationIndex());

        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;

        switch (id) {
            case R.id.menuoption_about:
                Log.d(TAG, "selected the about menu option");
                intent = new Intent(this, AboutActionBarActivity.class);
                intent.putExtra(getString(R.string.actionbar_titletext_key), "| About");
                startActivity(intent);

                return true;

            case R.id.menuoption_settings:
                Log.d(TAG, "selected the settings about menu option");
                intent = new Intent(this, SettingsActionBarActivity.class);
                intent.putExtra(getString(R.string.actionbar_titletext_key), "| Settings");
                startActivity(intent);

                return true;

            case R.id.menuoption_leavefeedback:
                Log.d(TAG, "selected the leavefeedback menu option");
                intent = new Intent(this, AppFeedbackFormActivity.class);
                intent.putExtra(getString(R.string.actionbar_titletext_key), "| App Feedback");
                startActivity(intent);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());

        String sectionTitle = getResources().getStringArray(R.array.nav_main_items)[(tab.getPosition())];

        View customActionBarView = getSupportActionBar().getCustomView();
        TextView sectionName = (TextView)customActionBarView.findViewById(R.id.actionbar_customtitle_sectionname);

        sectionName.setText(sectionTitle);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;

            switch (position) {
                case 0: // Realtime
                    fragment = new RealtimeMenuFragment();

                    break;
                case 1: // Schedules
                    fragment = new SchedulesListFragment();

                    break;
                case 2: // Transit Map
                    fragment = new TransitMapImageViewFragment();

                    break;
                case 3: // Connect
                    fragment = new ConnectListFragment();

                    break;
                default:
                    Log.d(TAG, "case default");

                    break;
            }

            return fragment;
        }

        @Override
        public int getCount() {

            return getResources().getStringArray(R.array.nav_main_items).length;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return "Title "+position;
        }
    }
}