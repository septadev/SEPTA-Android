package org.septa.android.app.nextarrive;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.septa.android.app.R;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.nextarrive.railstationpicker.RailStationPickerFragment;
import org.septa.android.app.support.BaseTabActivityHandler;
import org.septa.android.app.support.Consumer;


/**
 * Created by jkampf on 7/29/17.
 */

public class RailTabActivityHandler extends BaseTabActivityHandler  {
    private static final String TAG = "RailTabActivityHandler";

    public RailTabActivityHandler(String title) {
        super(title);
    }


    @Override
    public Fragment getFragment() {
        return RailTabActivityHandler.PlaceholderFragment.newInstance();
    }


    public static class PlaceholderFragment extends Fragment {
        public static PlaceholderFragment newInstance() {
            PlaceholderFragment fragment = new PlaceholderFragment();
            return fragment;
        }

        private StopModel startingStation;
        private StopModel endingStation;

        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.rail_next_to_arrive, container, false);

            final EditText startingStationEditText = (EditText) rootView.findViewById(R.id.starting_rail_station);
            final EditText endingStationEditText = (EditText) rootView.findViewById(R.id.ending_rail_station);

            startingStationEditText.setOnTouchListener(new StationPickerOnTouchListener(this, new Consumer<StopModel>() {
                        @Override
                        public void accept(StopModel var1) {
                            startingStation = var1;
                            startingStationEditText.setText(var1.getStopName());
                        }
                    })
            );

            endingStationEditText.setOnTouchListener(new StationPickerOnTouchListener(this, new Consumer<StopModel>() {
                        @Override
                        public void accept(StopModel var1) {
                            endingStation = var1;
                            endingStationEditText.setText(var1.getStopName());
                        }
                    })
            );

            return rootView;
        }

        public static class StationPickerOnTouchListener implements View.OnTouchListener {
            private Fragment parent;
            private Consumer<StopModel> consumer;

            StationPickerOnTouchListener(Fragment parent, Consumer<StopModel> consumer) {
                this.parent = parent;
                this.consumer = consumer;
            }

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d(TAG, "onTouch");
                int action = motionEvent.getActionMasked();
                if (action == MotionEvent.ACTION_UP) {
                    Log.d(TAG,
                            parent.getActivity().getClass().getCanonicalName());

                    FragmentTransaction ft = parent.getFragmentManager().beginTransaction();
                    Fragment prev = parent.getFragmentManager().findFragmentByTag("dialog");
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);

                    // Create and show the dialog.
                    RailStationPickerFragment newFragment = RailStationPickerFragment.newInstance(consumer);
                    newFragment.show(ft, "dialog");

                    return true;
                }
                return false;
            }
        }
    }
}
