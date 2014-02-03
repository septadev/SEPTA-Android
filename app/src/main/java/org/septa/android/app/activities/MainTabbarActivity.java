/*
 * MainTabbarActivity.java
 * Last modified on 01-29-2014 14:46-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

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
import org.septa.android.app.fragments.TransitMapImageViewFragment;
import org.septa.android.app.fragments.TransitMapWebViewFragment;

public class MainTabbarActivity extends BaseAnalyticsActionBarActivity implements ActionBar.TabListener {
    private static final String TAG = "MainTabbarActivity";

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
            Log.d(TAG, "doing the tabs... tab name " + tab);
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(tab)
                            .setTabListener(this)
            );
        }
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

        if (id == R.id.menuoption_about) {
            Log.d(TAG, "selected the about menu option.");
        } else {
            if (id == R.id.menuoption_settings) {
                Log.d(TAG, "selected the settings menu option");
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());

        Log.d(TAG, "onTabSelected: the tab position is this "+tab.getPosition());

        String sectionTitle = getResources().getStringArray(R.array.nav_main_items)[(tab.getPosition())];

        String mTitle = getResources().getString(R.string.titlebar_prefix_text) + " " + getResources().getString(R.string.titlebar_text_separator) + " " + sectionTitle;
        Log.d(TAG, "will change the title to " + mTitle);

        getSupportActionBar().setTitle(mTitle);
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
            Log.d(TAG, "in getItem with position" + position);
            Fragment fragment = null;

            switch (position) {
                case 0:
                    Log.d(TAG, "case 0, making and returning a placeholder fragment");
                    fragment = PlaceholderFragment.newInstance(position + 1);

                    break;
                case 1:
                    Log.d(TAG, "case 1, making and returning a placeholder fragment");
                    fragment = PlaceholderFragment.newInstance(position + 1);

                    break;
                case 2:
                    Log.d(TAG, "case 3, making and returning the map web view fragment");
//                    fragment = new TransitMapImageViewFragment();
                    fragment = new TransitMapWebViewFragment();

                    break;
                case 3:
                    Log.d(TAG, "case 4, making and returning the connect list fragment");
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }
}
