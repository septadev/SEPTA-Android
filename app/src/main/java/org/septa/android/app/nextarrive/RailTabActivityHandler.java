package org.septa.android.app.nextarrive;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.gms.location.FusedLocationProviderClient;

import org.septa.android.app.R;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.nextarrive.railstationpicker.RailStationPickerFragment;
import org.septa.android.app.nextarrive.railstationpicker.RailStationQuery;
import org.septa.android.app.nextarrive.railstationpicker.RailStationResults;
import org.septa.android.app.support.BaseTabActivityHandler;
import org.septa.android.app.support.BiConsumer;
import org.septa.android.app.support.Consumer;


/**
 * Created by jkampf on 7/29/17.
 */

public class RailTabActivityHandler extends BaseTabActivityHandler {
    private static final String TAG = "RailTabActivityHandler";
    FusedLocationProviderClient mFusedLocationClient;

    public RailTabActivityHandler(String title, FusedLocationProviderClient mFusedLocationClient) {
        super(title);
        this.mFusedLocationClient = mFusedLocationClient;
    }


    @Override
    public Fragment getFragment() {
        return RailTabActivityHandler.PlaceholderFragment.newInstance(mFusedLocationClient);
    }


    public static class PlaceholderFragment extends Fragment {
        public static final String RAIL_QUERY = "RailQuery";
        public static final String RAIL_RESULTS = "RailResults";
        private FragmentManager manager;

        FusedLocationProviderClient mFusedLocationClient;

        public static PlaceholderFragment newInstance(FusedLocationProviderClient mFusedLocationClient) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            fragment.setmFusedLocationClient(mFusedLocationClient);
            return fragment;
        }

        public void setmFusedLocationClient(FusedLocationProviderClient mFusedLocationClient) {
            this.mFusedLocationClient = mFusedLocationClient;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View fragementView = inflater.inflate(R.layout.fragment_holder, container, false);
            manager = getChildFragmentManager();

            manager.beginTransaction().add(R.id.fragment_content, RailStationQuery.newInstance(new BiConsumer<StopModel, StopModel>() {
                @Override
                public void accept(StopModel var1, StopModel var2) {
                    manager.beginTransaction().replace(R.id.fragment_content, RailStationResults.newInstance(var1, var2, mFusedLocationClient), RAIL_RESULTS).addToBackStack(null).commit();

                }
            }), RAIL_QUERY).addToBackStack(null).commit();

            return fragementView;
        }
    }


}
