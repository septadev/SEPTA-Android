package app.android.septa.org.septa_android.nextarrive;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import septa.org.android.nextarrive.railstationpicker.RailStationPickerFragment;
import septa.org.android.nextarrive.railstationpicker.StationPickNotifier;
import septa.org.android.support.BaseTabActivityHandler;
import septa.org.android2.R;

/**
 * Created by jkampf on 7/29/17.
 */

public class RailTabActivityHandler extends BaseTabActivityHandler implements StationPickNotifier {
    private static final String TAG = "RailTabActivityHandler";

    public RailTabActivityHandler(String title) {
        super(title);
    }

    private EditText targetEdit;

    @Override
    public Fragment getFragment() {
        return RailTabActivityHandler.PlaceholderFragment.newInstance();
    }

    @Override
    public void stationPicked(String name) {

    }

    public static class PlaceholderFragment extends Fragment {
        public static PlaceholderFragment newInstance() {
            PlaceholderFragment fragment = new PlaceholderFragment();
            return fragment;
        }

        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.rail_next_to_arrive, container, false);

            final EditText startingStationEditText = (EditText) rootView.findViewById(R.id.starting_rail_station);
            final EditText endingStationEditText = (EditText) rootView.findViewById(R.id.ending_rail_station);

            startingStationEditText.setOnTouchListener(new View.OnTouchListener() {
                                                           @Override
                                                           public boolean onTouch(View view, MotionEvent motionEvent) {
                                                               Log.d(TAG, "onTouch");
                                                               int action = motionEvent.getActionMasked();
                                                               if (action == MotionEvent.ACTION_UP) {
                                                                   Log.d(TAG,
                                                                           getActivity().getClass().getCanonicalName());

                                                                   FragmentTransaction ft = getFragmentManager().beginTransaction();
                                                                   Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                                                                   if (prev != null) {
                                                                       ft.remove(prev);
                                                                   }
                                                                   ft.addToBackStack(null);

                                                                   // Create and show the dialog.
                                                                   RailStationPickerFragment newFragment = RailStationPickerFragment.newInstance();
                                                                   newFragment.setTarget(startingStationEditText);
                                                                   newFragment.show(ft, "dialog");

                                                                   return true;
                                                               }
                                                               return false;
                                                           }
                                                       }
            );


            return rootView;
        }
    }
}
