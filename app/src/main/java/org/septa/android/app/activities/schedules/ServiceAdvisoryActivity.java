/**
 * Created by acampbell on 11/3/14.
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities.schedules;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;

import org.septa.android.app.R;
import org.septa.android.app.activities.BaseAnalyticsActionBarActivity;
import org.septa.android.app.fragments.AdvisoryFragment;
import org.septa.android.app.fragments.DetourFragment;
import org.septa.android.app.models.RouteTypes;
import org.septa.android.app.models.servicemodels.ServiceAdvisoryModel;

import java.util.ArrayList;
import java.util.List;

public class ServiceAdvisoryActivity extends BaseAnalyticsActionBarActivity {
    private static String TAG = ServiceAdvisoryActivity.class.getName();

    private ArrayList<ServiceAdvisoryModel> alerts;
    private String routeShortName;
    private RouteTypes routeType;

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_advisory_activity);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        viewPager = (ViewPager) findViewById(R.id.view_pager);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            alerts = extras.getParcelableArrayList(getString(R.string.alerts_extra_alerts));
            actionBar.setTitle(extras.getString(getString(R.string.alerts_extra_title)));
            actionBar.setIcon(extras.getInt(getString(R.string.alerts_extra_icon)));
            routeType = (RouteTypes) extras.getSerializable(getString(R.string.alerts_extra_route_type));

            if(alerts != null) {
                ActionBar.TabListener tabListener = new ActionBar.TabListener() {
                    @Override
                    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                        viewPager.setCurrentItem(tab.getPosition());
                    }

                    @Override
                    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

                    }

                    @Override
                    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

                    }
                };

                ViewPager.SimpleOnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        actionBar.selectTab(actionBar.getTabAt(position));
                    }
                };

                AdvisoryFragmentPagerAdapter fragmentPagerAdapter =
                        new AdvisoryFragmentPagerAdapter(getSupportFragmentManager());

                if (ServiceAdvisoryModel.hasValidAdvisory(alerts)) {
                    actionBar.addTab(actionBar.newTab().setText(getString(R.string.advisory_tab_title))
                            .setTabListener(tabListener).setIcon(R.drawable.ic_system_status_advisory));
                    fragmentPagerAdapter.addFragment(
                            AdvisoryFragment.newInstance(ServiceAdvisoryModel.getAdvisoryMessage(alerts)));
                }
                if (ServiceAdvisoryModel.hasValidDetours(alerts)) {
                    actionBar.addTab(actionBar.newTab().setText(getString(R.string.detour_tab_title))
                            .setTabListener(tabListener).setIcon(R.drawable.ic_system_status_detour));
                    fragmentPagerAdapter.addFragment(
                            DetourFragment.newInstance(ServiceAdvisoryModel.getDetours(alerts)));
                }

                viewPager.setOnPageChangeListener(pageChangeListener);
                viewPager.setAdapter(fragmentPagerAdapter);
            }
        }
    }

    private class AdvisoryFragmentPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments;

        public AdvisoryFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<Fragment>();
        }

        public void addFragment(Fragment fragment) {
            fragments.add(fragment);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
