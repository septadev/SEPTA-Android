package org.septa.android.app.nextarrive.locationpicker;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.septa.android.app.R;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.support.Consumer;
import org.septa.android.app.support.CursorAdapterSupplier;
import org.septa.android.app.support.TabActivityHandler;


/**
 * Created by jkampf on 7/30/17.
 */

public class LocationPickerFragment extends DialogFragment implements NavigationView.OnNavigationItemSelectedListener {
    public static final String TAG = "RailStationPickerFrag";

    private LocationPickerFragment.SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    TabActivityHandler tabActivityHandlers[];
    private EditText target;

    int selectedTab = 0;


    private StopModel[] currentStop;

    private Consumer<StopModel> consumer;


    private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;

    @Override
    public void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog");

        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.by_station, null);

        currentStop = new StopModel[2];
        tabActivityHandlers = new TabActivityHandler[2];
        tabActivityHandlers[0] = new ByStopTabActivityHandler("BY STATION", new Consumer<StopModel>() {
            @Override
            public void accept(StopModel var1) {
                currentStop[0] = var1;
            }
        }, cursorAdapterSupplier);

        tabActivityHandlers[1] = new ByAddressTabActivityHandler("BY ADDRESS", new Consumer<StopModel>() {
            @Override
            public void accept(StopModel var1) {
                currentStop[1] = var1;
            }
        }, cursorAdapterSupplier);

        mSectionsPagerAdapter = new LocationPickerFragment.SectionsPagerAdapter(getChildFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) dialogView.findViewById(R.id.station_container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) dialogView.findViewById(R.id.station_picker_tab);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = tab.getPosition();
                Log.d(TAG, "onTabSelected");
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.d(TAG, "onTabReselected");
                selectedTab = tab.getPosition();
            }
        });

        Button cancel = (Button) dialogView.findViewById(R.id.cancel_button);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Current Possition is:" + selectedTab);
                getDialog().dismiss();
            }
        });

        Button select = (Button) dialogView.findViewById(R.id.select_button);
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Current Possition is:" + selectedTab);

                if (currentStop[selectedTab] == null) {
                    Log.d(TAG, "No Station");
                    return;
                }
                Log.d(TAG, "Station is:" + currentStop[selectedTab].getStopName());
                consumer.accept(currentStop[selectedTab]);
                getDialog().dismiss();
            }
        });


        Log.d(TAG, "End - onCreateView");
        return dialogView;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    public void setConsumer(Consumer<StopModel> consumer) {
        this.consumer = consumer;
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

    public static LocationPickerFragment newInstance(Consumer<StopModel> consumer, CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
        LocationPickerFragment fragment = new LocationPickerFragment();
        fragment.setConsumer(consumer);
        fragment.setCursorAdapterSupplier(cursorAdapterSupplier);
        return fragment;
    }

    public void setCursorAdapterSupplier(CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
        this.cursorAdapterSupplier = cursorAdapterSupplier;
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



}
