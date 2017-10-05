package org.septa.android.app.locationpicker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.support.CursorAdapterSupplier;
import org.septa.android.app.support.TabActivityHandler;


/**
 * Created by jkampf on 7/30/17.
 */

public class LocationPickerFragment extends DialogFragment {
    public static final String TAG = "LocationPickerFragment";
    public static final int SUCCESS = 0;
    public static final String STOP_MODEL = "stopModel";
    private static final int STOP_MODEL_REQUEST = 1;

    TabActivityHandler tabActivityHandlers[];

    private TextView searchByStationTab;
    private TextView searchByAddressTab;


    private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;
    private LocationPickerCallBack locationPickerCallBack;

    @Override
    public void onResume() {
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog");
        restoreArgs();

        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.by_station, null);

        tabActivityHandlers = new TabActivityHandler[2];
        tabActivityHandlers[0] = new ByStopTabActivityHandler("BY STATION", cursorAdapterSupplier);

        tabActivityHandlers[1] = new ByAddressTabActivityHandler("BY ADDRESS", cursorAdapterSupplier);


        searchByStationTab = (TextView) dialogView.findViewById(R.id.search_by_station_tab);
        searchByAddressTab = (TextView) dialogView.findViewById(R.id.search_by_address_tab);

        setActive(searchByStationTab, searchByAddressTab);
        Fragment searchByStationTabFragment = tabActivityHandlers[0].getFragment();
        searchByStationTabFragment.setTargetFragment(this, STOP_MODEL_REQUEST);
        getChildFragmentManager()
                .beginTransaction().replace(R.id.stop_picker_container, searchByStationTabFragment).commit();

        dialogView.findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });


        searchByStationTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setActive(searchByStationTab, searchByAddressTab);
                Fragment tabFragment = tabActivityHandlers[0].getFragment();
                tabFragment.setTargetFragment(LocationPickerFragment.this, STOP_MODEL_REQUEST);
                getChildFragmentManager()
                        .beginTransaction().replace(R.id.stop_picker_container, tabFragment).commit();
            }
        });

        searchByAddressTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setActive(searchByAddressTab, searchByStationTab);
                Fragment tabFragment = tabActivityHandlers[1].getFragment();
                tabFragment.setTargetFragment(LocationPickerFragment.this, STOP_MODEL_REQUEST);
                getChildFragmentManager()
                        .beginTransaction().replace(R.id.stop_picker_container, tabFragment).commit();

            }
        });

//        Button select = (Button) dialogView.findViewById(R.id.select_button);
//        select.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d(TAG, "Current Possition is:" + selectedTab);
//
//                if (currentStop[selectedTab] == null) {
//                    Log.d(TAG, "No Station");
//                    return;
//                }
//                Log.d(TAG, "Station is:" + currentStop[selectedTab].getStopName());
//                consumer.accept(currentStop[selectedTab]);
//                getDialog().dismiss();
//            }
//        });


        Log.d(TAG, "End - onCreateView");
        return dialogView;
    }

    private void setActive(TextView active, TextView inactive) {
        active.setBackgroundResource(R.drawable.bg_stop_picker_active);
        inactive.setBackgroundResource(R.drawable.bg_stop_picker_inactive);

        active.setTextColor(ContextCompat.getColor(getContext(), R.color.find_station_tab_active_text));
        inactive.setTextColor(ContextCompat.getColor(getContext(), R.color.find_station_tab_inactive_text));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //do what ever you want here, and get the result from intent like below
        if (requestCode == STOP_MODEL_REQUEST && resultCode == LocationPickerFragment.SUCCESS) {
            StopModel var1 = (StopModel) data.getSerializableExtra(LocationPickerFragment.STOP_MODEL);
            if (locationPickerCallBack != null) {
                locationPickerCallBack.setLocation(var1);
            } else if (getTargetFragment() != null) {
                Intent intent = new Intent();
                intent.putExtra(STOP_MODEL, var1);
                getTargetFragment().onActivityResult(getTargetRequestCode(), SUCCESS, intent);
            }
            dismiss();

            return;
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if ((context instanceof LocationPickerCallBack)) {
            locationPickerCallBack = (LocationPickerCallBack) context;
        }

    }


    public static LocationPickerFragment newInstance(CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
        LocationPickerFragment fragment = new LocationPickerFragment();

        Bundle args = new Bundle();
        args.putSerializable("cursorAdapterSupplier", cursorAdapterSupplier);
        fragment.setArguments(args);

        return fragment;
    }


    private void restoreArgs() {
        cursorAdapterSupplier = (CursorAdapterSupplier<StopModel>) getArguments().getSerializable("cursorAdapterSupplier");
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


}
