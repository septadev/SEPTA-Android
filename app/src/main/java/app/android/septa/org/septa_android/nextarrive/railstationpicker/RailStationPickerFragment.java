package app.android.septa.org.septa_android.nextarrive.railstationpicker;

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

import app.android.septa.org.septa_android.R;
import app.android.septa.org.septa_android.support.TabActivityHandler;


/**
 * Created by jkampf on 7/30/17.
 */

public class RailStationPickerFragment extends DialogFragment implements NavigationView.OnNavigationItemSelectedListener {
    public static final String TAG = "RailStationPickerFrag";

    private RailStationPickerFragment.SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    TabActivityHandler tabActivityHandlers[];
    private EditText target;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog");

        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.by_station, null);

        tabActivityHandlers = new TabActivityHandler[2];
        tabActivityHandlers[0] = new ByStationTabActivityHandler("BY STATION", target);
        tabActivityHandlers[1] = new ByAddressTabActivityHandler("BY ADDRESS", target);

        mSectionsPagerAdapter = new RailStationPickerFragment.SectionsPagerAdapter(getChildFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) dialogView.findViewById(R.id.station_container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) dialogView.findViewById(R.id.station_picker_tab);
        tabLayout.setupWithViewPager(mViewPager);

        Button cancel = (Button) dialogView.findViewById(R.id.cancel_button);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        Button select = (Button) dialogView.findViewById(R.id.select_button);
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

    public void setTarget(EditText target) {
        this.target = target;
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

    public static RailStationPickerFragment newInstance() {
        return new RailStationPickerFragment();
    }
}
