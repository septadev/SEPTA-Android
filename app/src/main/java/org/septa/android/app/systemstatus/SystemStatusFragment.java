package org.septa.android.app.systemstatus;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.database.DatabaseManager;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.support.TabActivityHandler;


public class SystemStatusFragment extends Fragment {

    public static final String TAG = SystemStatusFragment.class.getSimpleName();

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if (getActivity() == null) {
            return null;
        }

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        DatabaseManager dbManager = DatabaseManager.getInstance(getActivity());

        tabActivityHandlers = new TabActivityHandler[5];
        tabActivityHandlers[1] = new SystemStatusLineTabHandler(getString(R.string.tab_rail), TransitType.RAIL, dbManager.getRailNoDirectionRouteCursorAdapterSupplier());
        tabActivityHandlers[0] = new SystemStatusLineTabHandler(getString(R.string.tab_bus), TransitType.BUS, dbManager.getBusNoDirectionRouteCursorAdapterSupplier());
        tabActivityHandlers[3] = new SystemStatusLineTabHandler(getString(R.string.tab_trolley), TransitType.TROLLEY, dbManager.getTrolleyNoDirectionRouteCursorAdapterSupplier());
        tabActivityHandlers[2] = new SystemStatusLineTabHandler(getString(R.string.tab_subway), TransitType.SUBWAY, dbManager.getSubwayNoDirectionRouteCursorAdapterSupplier());
        tabActivityHandlers[4] = new SystemStatusLineTabHandler(getString(R.string.tab_nhsl), TransitType.NHSL, new RouteDirectionModel("NHSL", "NHSL", "Norristown TC to 69th St TC", null, null, 0));

        View fragmentView = inflater.inflate(R.layout.fragment_next_to_arrive, null);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) fragmentView.findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) fragmentView.findViewById(R.id.tabs);
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
