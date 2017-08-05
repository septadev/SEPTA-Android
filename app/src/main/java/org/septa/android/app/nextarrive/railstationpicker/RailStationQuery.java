package org.septa.android.app.nextarrive.railstationpicker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.septa.android.app.R;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.nextarrive.RailTabActivityHandler;
import org.septa.android.app.support.BiConsumer;
import org.septa.android.app.support.Consumer;
import org.septa.android.app.support.CursorAdapterSupplier;

/**
 * Created by jkampf on 8/3/17.
 */

public class RailStationQuery extends Fragment {
    private StopModel startingStation;
    private StopModel endingStation;


    private BiConsumer<StopModel, StopModel> consumer;

    private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;

    public static RailStationQuery newInstance(BiConsumer<StopModel, StopModel> consumer, CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
        RailStationQuery fragment = new RailStationQuery();
        fragment.setConsumer(consumer);
        fragment.setCursorAdapterSupplier(cursorAdapterSupplier);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.rail_next_to_arrive_search, container, false);

        final EditText startingStationEditText = (EditText) rootView.findViewById(R.id.starting_rail_station);
        final EditText endingStationEditText = (EditText) rootView.findViewById(R.id.ending_rail_station);
        final Button queryButton = (Button) rootView.findViewById(R.id.view_trains_button);

        startingStationEditText.setOnTouchListener(new RailStationQuery.StationPickerOnTouchListener(this, new Consumer<StopModel>() {
                    @Override
                    public void accept(StopModel var1) {
                        startingStation = var1;
                        startingStationEditText.setText(var1.getStopName());
                    }
                }, cursorAdapterSupplier)
        );

        endingStationEditText.setOnTouchListener(new RailStationQuery.StationPickerOnTouchListener(this, new Consumer<StopModel>() {
                    @Override
                    public void accept(StopModel var1) {
                        endingStation = var1;
                        endingStationEditText.setText(var1.getStopName());
                    }
                }, cursorAdapterSupplier)
        );

        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (startingStation == null || endingStation == null) {
                    Toast.makeText(getActivity(), "Need to choose a start and end station.", Toast.LENGTH_SHORT).show();
                    return;
                }
                consumer.accept(startingStation, endingStation);
            }
        });


        return rootView;
    }

    public static class StationPickerOnTouchListener implements View.OnTouchListener {
        private Fragment parent;
        private Consumer<StopModel> consumer;
        private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;

        StationPickerOnTouchListener(Fragment parent, Consumer<StopModel> consumer, CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
            this.parent = parent;
            this.consumer = consumer;
            this.cursorAdapterSupplier = cursorAdapterSupplier;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int action = motionEvent.getActionMasked();
            if (action == MotionEvent.ACTION_UP) {

                FragmentTransaction ft = parent.getFragmentManager().beginTransaction();
                Fragment prev = parent.getFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                // Create and show the dialog.
                RailStationPickerFragment newFragment = RailStationPickerFragment.newInstance(consumer, cursorAdapterSupplier);
                newFragment.show(ft, "dialog");

                return true;
            }
            return false;
        }
    }

    public void setConsumer(BiConsumer<StopModel, StopModel> consumer) {
        this.consumer = consumer;
    }

    public void setCursorAdapterSupplier(CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
        this.cursorAdapterSupplier = cursorAdapterSupplier;
    }

}
