package org.septa.android.app.nextarrive;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.septa.android.app.R;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.nextarrive.railstationpicker.RailStationQuery;
import org.septa.android.app.support.BaseTabActivityHandler;
import org.septa.android.app.support.BiConsumer;
import org.septa.android.app.support.CursorAdapterSupplier;


/**
 * Created by jkampf on 7/29/17.
 */

public class RailTabActivityHandler extends BaseTabActivityHandler {
    private static final String TAG = "RailTabActivityHandler";
    private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;

    public RailTabActivityHandler(String title, CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
        super(title);
        this.cursorAdapterSupplier = cursorAdapterSupplier;
    }


    @Override
    public Fragment getFragment() {
        return RailTabActivityHandler.PlaceholderFragment.newInstance(cursorAdapterSupplier);
    }


    public static class PlaceholderFragment extends Fragment {
        public static final String RAIL_QUERY = "RailQuery";
        public static final String RAIL_RESULTS = "RailResults";
        private FragmentManager manager;

        CursorAdapterSupplier<StopModel> cursorAdapterSupplier;

        public static PlaceholderFragment newInstance(CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            fragment.setCursorAdapterSupplier(cursorAdapterSupplier);
            return fragment;
        }

        public void setCursorAdapterSupplier(CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
            this.cursorAdapterSupplier = cursorAdapterSupplier;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View fragementView = inflater.inflate(R.layout.fragment_holder, container, false);
            manager = getChildFragmentManager();

            manager.beginTransaction().add(R.id.fragment_content, RailStationQuery.newInstance(new BiConsumer<StopModel, StopModel>() {
                @Override
                public void accept(StopModel var1, StopModel var2) {
                    Intent intent = new Intent(getActivity(), RailStationNextToArriveResults.class);
                    intent.putExtra(RailStationNextToArriveResults.STARTING_STATION, var1);
                    intent.putExtra(RailStationNextToArriveResults.DESTINATAION_STATION, var2);

                    startActivity(intent);
                    //manager.beginTransaction().replace(R.id.fragment_content, RailStationNextToArriveResults.newInstance(var1, var2), RAIL_RESULTS).addToBackStack(null).commit();
                }
            }, cursorAdapterSupplier), RAIL_QUERY).addToBackStack(null).commit();

            return fragementView;
        }
    }


}
