package org.septa.android.app.schedules;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.database.DatabaseManager;
import org.septa.android.app.locationpicker.LineAwareLocationPickerTabActivityHandler;

/************************************************************************************************************
 * Class: SchedulesFragment
 * Purpose: The Schedule adapter class manages the Transit Schedules Fragment in the application
 * Created by ttuggerson on 8/31/17.
 */

public class SchedulesFragment extends Fragment {

    public static final String TAG = SchedulesFragment.class.getSimpleName();

    private SchedulesFragment.SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout tabLayout;
    LineAwareLocationPickerTabActivityHandler tabActivityHandlers[];
    int startingIndex = 0;

    Bundle prePopulated = null;

    public static SchedulesFragment newInstance() {
        SchedulesFragment instance = new SchedulesFragment();
        return instance;
    }

    @Override
    public void onResume() {
        super.onResume();

        ((TextView) tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getCustomView())
                .setCompoundDrawablesWithIntrinsicBounds(tabActivityHandlers[tabLayout.getSelectedTabPosition()]
                        .getActiveDrawableId(), 0, 0, 0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (getActivity() == null)
            return  null;

        DatabaseManager dbManager = DatabaseManager.getInstance(getActivity());

        tabActivityHandlers = new LineAwareLocationPickerTabActivityHandler[5];
        tabActivityHandlers[1] = new LineAwareLocationPickerTabActivityHandler(getString(R.string.rail_tab), "nta_picker_title", getString(R.string.schedule_query_button_text), TransitType.RAIL, dbManager.getRailRouteCursorAdapaterSupplier(), dbManager.getLineAwareRailStopCursorAdapterSupplier(), dbManager.getLineAwareRailStopAfterCursorAdapterSupplier(), ScheduleResultsActivity.class);
        tabActivityHandlers[0] = new LineAwareLocationPickerTabActivityHandler(getString(R.string.bus_tab), "nta_picker_title", getString(R.string.schedule_query_button_text), TransitType.BUS, dbManager.getBusRouteCursorAdapterSupplier(), dbManager.getBusStopCursorAdapterSupplier(), dbManager.getBusStopAfterCursorAdapterSupplier(), ScheduleResultsActivity.class);
        tabActivityHandlers[3] = new LineAwareLocationPickerTabActivityHandler(getString(R.string.trolley_tab), "nta_picker_title", getString(R.string.schedule_query_button_text), TransitType.TROLLEY, dbManager.getTrolleyRouteCursorAdapterSupplier(), dbManager.getTrolleyStopCursorAdapterSupplier(), dbManager.getTrolleyStopAfterCursorAdapterSupplier(), ScheduleResultsActivity.class);
        tabActivityHandlers[2] = new LineAwareLocationPickerTabActivityHandler(getString(R.string.subway_tab), "nta_picker_title", getString(R.string.schedule_query_button_text), TransitType.SUBWAY, dbManager.getSubwayRouteCursorAdapterSupplier(), dbManager.getSubwayStopCursorAdapterSupplier(), dbManager.getSubwayStopAfterCursorAdapterSupplier(), ScheduleResultsActivity.class);
        tabActivityHandlers[4] = new LineAwareLocationPickerTabActivityHandler(getString(R.string.nhsl_tab), "nta_picker_title", getString(R.string.schedule_query_button_text), TransitType.NHSL, dbManager.getNHSLRouteCursorAdapterSupplier(), dbManager.getBusStopCursorAdapterSupplier(), dbManager.getBusStopAfterCursorAdapterSupplier(), ScheduleResultsActivity.class);

        if (prePopulated != null) {
            tabActivityHandlers[startingIndex].setPrepopulate(prePopulated);
        }

        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);


        View fragmentView = inflater.inflate(R.layout.schedule_fragement_main, null);

        mSectionsPagerAdapter = new SchedulesFragment.SectionsPagerAdapter(getChildFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) fragmentView.findViewById(R.id.schedule_fragment_container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) fragmentView.findViewById(R.id.schedule_fragment_tabs);
        tabLayout.setupWithViewPager(mViewPager);
        setUpTabs(tabLayout, inflater);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ((TextView) tab.getCustomView()).setCompoundDrawablesWithIntrinsicBounds(tabActivityHandlers[tab.getPosition()].getActiveDrawableId(), 0, 0, 0);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                ((TextView) tab.getCustomView()).setCompoundDrawablesWithIntrinsicBounds(tabActivityHandlers[tab.getPosition()].getInactiveDrawableId(), 0, 0, 0);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        tabLayout.getTabAt(startingIndex).select();

        return fragmentView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("org.septa.android.app.nextarrive.NextToArriveFragment.mSectionsPagerAdapter", mSectionsPagerAdapter.saveState());
        outState.putString("title", getActivity().getTitle().toString());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            Parcelable parcelable = savedInstanceState.getParcelable("org.septa.android.app.nextarrive.NextToArriveFragment.mSectionsPagerAdapter");
            if (parcelable != null)
                mSectionsPagerAdapter.restoreState(parcelable, this.getClass().getClassLoader());
            String title = savedInstanceState.getString("title");
            if (title != null && getActivity() != null)
                getActivity().setTitle(title);
        }
    }

    private void setUpTabs(TabLayout tabLayout, LayoutInflater inflater) {
        for (int i = 0; i < tabActivityHandlers.length; i++) {
            TextView tab = (TextView) inflater.inflate(R.layout.custom_tab, null);
            tab.setText(tabActivityHandlers[i].getTabTitle());
            tab.setCompoundDrawablesWithIntrinsicBounds(tabActivityHandlers[i].getInactiveDrawableId(), 0, 0, 0);
            tab.setContentDescription("Tap to switch to " + tabActivityHandlers[i].getTabTitle());
            tabLayout.getTabAt(i).setCustomView(tab);
        }
    }

    public void prePopulate(Bundle data) {
        TransitType transitType = (TransitType) data.get(Constants.TRANSIT_TYPE);
        if (transitType == TransitType.BUS) {
            startingIndex = 0;
        } else if (transitType == TransitType.RAIL) {
            startingIndex = 1;
        } else if (transitType == TransitType.TROLLEY) {
            startingIndex = 3;
        } else if (transitType == TransitType.SUBWAY) {
            startingIndex = 2;
        } else {
            startingIndex = 4;
        }

        prePopulated = data;

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

