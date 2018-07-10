package org.septa.android.app.nextarrive;

import android.Manifest;
import android.app.Activity;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.database.DatabaseManager;
import org.septa.android.app.locationpicker.LineAwareLocationPickerTabActivityHandler;
import org.septa.android.app.locationpicker.LineUnawareLocationPickerTabActivityHandler;
import org.septa.android.app.support.TabActivityHandler;


public class NextToArriveFragment extends Fragment {

    public static final String TAG = NextToArriveFragment.class.getSimpleName(),
            NTA_KEY_SECTIONS_PAGER_ADAPTER = "NTA_KEY_SECTIONS_PAGER_ADAPTER",
            NTA_ACTIVITY_TITLE = "NTA_ACTIVITY_TITLE";

    private static final String TAB_HEADER_STRING_NAME = "nta_picker_title";
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout tabLayout;
    TabActivityHandler tabActivityHandlers[];

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

        Activity context = getActivity();

        if (context == null) {
            return null;
        }

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        DatabaseManager dbManager = DatabaseManager.getInstance(context);

        tabActivityHandlers = new TabActivityHandler[5];
        tabActivityHandlers[1] = new LineUnawareLocationPickerTabActivityHandler(getString(R.string.tab_rail), TAB_HEADER_STRING_NAME, getString(R.string.nta_query_button_text), TransitType.RAIL, dbManager.getRailStopCursorAdapterSupplier(), NextToArriveResultsActivity.class);
        tabActivityHandlers[0] = new LineAwareLocationPickerTabActivityHandler(getString(R.string.tab_bus), TAB_HEADER_STRING_NAME, getString(R.string.nta_query_button_text), TransitType.BUS, dbManager.getBusRouteCursorAdapterSupplier(), dbManager.getBusStopCursorAdapterSupplier(), dbManager.getBusStopAfterCursorAdapterSupplier(), NextToArriveResultsActivity.class);
        tabActivityHandlers[3] = new LineAwareLocationPickerTabActivityHandler(getString(R.string.tab_trolley), TAB_HEADER_STRING_NAME, getString(R.string.nta_query_button_text), TransitType.TROLLEY, dbManager.getTrolleyRouteCursorAdapterSupplier(), dbManager.getTrolleyStopCursorAdapterSupplier(), dbManager.getTrolleyStopAfterCursorAdapterSupplier(), NextToArriveResultsActivity.class);
        tabActivityHandlers[2] = new LineAwareLocationPickerTabActivityHandler(getString(R.string.tab_subway), TAB_HEADER_STRING_NAME, getString(R.string.nta_query_button_text), TransitType.SUBWAY, dbManager.getSubwayRouteCursorAdapterSupplier(), dbManager.getSubwayStopCursorAdapterSupplier(), dbManager.getSubwayStopAfterCursorAdapterSupplier(), NextToArriveResultsActivity.class);
        tabActivityHandlers[4] = new LineAwareLocationPickerTabActivityHandler(getString(R.string.tab_nhsl), TAB_HEADER_STRING_NAME, getString(R.string.nta_query_button_text), TransitType.NHSL, dbManager.getNHSLRouteCursorAdapterSupplier(), dbManager.getBusStopCursorAdapterSupplier(), dbManager.getBusStopAfterCursorAdapterSupplier(), NextToArriveResultsActivity.class);

        View fragmentView = inflater.inflate(R.layout.fragment_next_to_arrive, null);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = fragmentView.findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = fragmentView.findViewById(R.id.tabs);
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

        return fragmentView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(NTA_KEY_SECTIONS_PAGER_ADAPTER, mSectionsPagerAdapter.saveState());
        outState.putString(NTA_ACTIVITY_TITLE, getActivity().getTitle().toString());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            Parcelable parcelable = savedInstanceState.getParcelable(NTA_KEY_SECTIONS_PAGER_ADAPTER);
            if (parcelable != null) {
                mSectionsPagerAdapter.restoreState(parcelable, this.getClass().getClassLoader());
            }
            String title = savedInstanceState.getString(NTA_ACTIVITY_TITLE);
            if (title != null && getActivity() != null) {
                getActivity().setTitle(title);
            }
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
