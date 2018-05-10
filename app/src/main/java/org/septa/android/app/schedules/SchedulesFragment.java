package org.septa.android.app.schedules;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.database.DatabaseManager;
import org.septa.android.app.locationpicker.LineAwareLocationPickerTabActivityHandler;

import java.util.List;

/************************************************************************************************************
 * Class: SchedulesFragment
 * Purpose: The Schedule adapter class manages the Transit Schedules Fragment in the application
 * Created by ttuggerson on 8/31/17.
 */

public class SchedulesFragment extends Fragment {

    public static final String TAG = SchedulesFragment.class.getSimpleName(),
        KEY_SCHEDULES_SECTIONS_PAGER_ADAPTER = "KEY_SCHEDULES_SECTIONS_PAGER_ADAPTER",
            KEY_SCHEDULES_FRAGMENT_TITLE = "KEY_SCHEDULES_FRAGMENT_TITLE";

    private static final String TAB_HEADER_STRING_NAME = "nta_picker_title",
        HOLIDAY_SCHEDULE_URL = "https://www.septa.org/schedules/modified-mobile",
        HOLIDAY_SCHEDULE_URL_RAIL = "https://septa.org/schedules/rail/special/holidays-mobile.html";

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

        List<TransitType> holidayTransitTypes = TransitType.transitTypesOnHolidayToday();
        if (holidayTransitTypes != null && holidayTransitTypes.size() > 0) {
            Activity activity = getActivity();
            if (activity != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.holiday_schedule_title);

                if (holidayTransitTypes.size() == TransitType.values().length) {
                    builder.setMessage(R.string.all_holiday_message);
                } else {
                    builder.setMessage(R.string.transit_weekday_message);
                }

                builder.setNeutralButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                if (holidayTransitTypes.contains(TransitType.BUS) || holidayTransitTypes.contains(TransitType.TROLLEY) || holidayTransitTypes.contains(TransitType.SUBWAY) || holidayTransitTypes.contains(TransitType.NHSL))
                    builder.setPositiveButton(R.string.holiday_alert_button_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Activity activity = getActivity();
                            if (activity != null) {
                                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(HOLIDAY_SCHEDULE_URL));
                                startActivity(myIntent);
                            }
                        }
                    });

                if (holidayTransitTypes.contains(TransitType.RAIL))
                    builder.setNegativeButton(R.string.holiday_alert_button_text_rail, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Activity activity = getActivity();
                            if (activity != null) {
                                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(HOLIDAY_SCHEDULE_URL_RAIL));
                                startActivity(myIntent);
                            }
                        }
                    });


                AlertDialog dialog = builder.create();
                dialog.show();
            }

        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (getActivity() == null)
            return null;

        DatabaseManager dbManager = DatabaseManager.getInstance(getActivity());

        tabActivityHandlers = new LineAwareLocationPickerTabActivityHandler[5];
        tabActivityHandlers[1] = new LineAwareLocationPickerTabActivityHandler(getString(R.string.tab_rail), TAB_HEADER_STRING_NAME, getString(R.string.schedule_query_button_text), TransitType.RAIL, dbManager.getRailRouteCursorAdapaterSupplier(), dbManager.getLineAwareRailStopCursorAdapterSupplier(), dbManager.getLineAwareRailStopAfterCursorAdapterSupplier(), ScheduleResultsActivity.class);
        tabActivityHandlers[0] = new LineAwareLocationPickerTabActivityHandler(getString(R.string.tab_bus), TAB_HEADER_STRING_NAME, getString(R.string.schedule_query_button_text), TransitType.BUS, dbManager.getBusRouteCursorAdapterSupplier(), dbManager.getBusStopCursorAdapterSupplier(), dbManager.getBusStopAfterCursorAdapterSupplier(), ScheduleResultsActivity.class);
        tabActivityHandlers[3] = new LineAwareLocationPickerTabActivityHandler(getString(R.string.tab_trolley), TAB_HEADER_STRING_NAME, getString(R.string.schedule_query_button_text), TransitType.TROLLEY, dbManager.getTrolleyRouteCursorAdapterSupplier(), dbManager.getTrolleyStopCursorAdapterSupplier(), dbManager.getTrolleyStopAfterCursorAdapterSupplier(), ScheduleResultsActivity.class);
        tabActivityHandlers[2] = new LineAwareLocationPickerTabActivityHandler(getString(R.string.tab_subway), TAB_HEADER_STRING_NAME, getString(R.string.schedule_query_button_text), TransitType.SUBWAY, dbManager.getSubwayRouteCursorAdapterSupplier(), dbManager.getSubwayStopCursorAdapterSupplier(), dbManager.getSubwayStopAfterCursorAdapterSupplier(), ScheduleResultsActivity.class);
        tabActivityHandlers[4] = new LineAwareLocationPickerTabActivityHandler(getString(R.string.tab_nhsl), TAB_HEADER_STRING_NAME, getString(R.string.schedule_query_button_text), TransitType.NHSL, dbManager.getNHSLRouteCursorAdapterSupplier(), dbManager.getBusStopCursorAdapterSupplier(), dbManager.getBusStopAfterCursorAdapterSupplier(), ScheduleResultsActivity.class);

        if (prePopulated != null) {
            tabActivityHandlers[startingIndex].setPrepopulate(prePopulated);
        }

        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);


        View fragmentView = inflater.inflate(R.layout.schedule_fragment_main, null);

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
        outState.putParcelable(KEY_SCHEDULES_SECTIONS_PAGER_ADAPTER, mSectionsPagerAdapter.saveState());
        outState.putString(KEY_SCHEDULES_FRAGMENT_TITLE, getActivity().getTitle().toString());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            Parcelable parcelable = savedInstanceState.getParcelable(KEY_SCHEDULES_SECTIONS_PAGER_ADAPTER);
            if (parcelable != null)
                mSectionsPagerAdapter.restoreState(parcelable, this.getClass().getClassLoader());
            String title = savedInstanceState.getString(KEY_SCHEDULES_FRAGMENT_TITLE);
            if (title != null && getActivity() != null)
                getActivity().setTitle(title);
        }
    }

    private void setUpTabs(TabLayout tabLayout, LayoutInflater inflater) {
        for (int i = 0; i < tabActivityHandlers.length; i++) {
            TextView tab = (TextView) inflater.inflate(R.layout.custom_tab, null);
            tab.setText(tabActivityHandlers[i].getTabTitle());
            tab.setCompoundDrawablesWithIntrinsicBounds(tabActivityHandlers[i].getInactiveDrawableId(), 0, 0, 0);
            tab.setContentDescription(getString(R.string.tab_description) + tabActivityHandlers[i].getTabTitle());
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

