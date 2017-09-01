package org.septa.android.app.locationpicker;

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
import android.view.WindowManager;
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

public class LocationPickerFragment extends DialogFragment {
    public static final String TAG = "LocationPickerFragment";

    TabActivityHandler tabActivityHandlers[];
    private Consumer<StopModel> consumer;


    private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;

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

        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.by_station, null);

        tabActivityHandlers = new TabActivityHandler[2];
        tabActivityHandlers[0] = new ByStopTabActivityHandler("BY STATION", new Consumer<StopModel>() {
            @Override
            public void accept(StopModel var1) {
                consumer.accept(var1);
                dismiss();
            }
        }, cursorAdapterSupplier);

        tabActivityHandlers[1] = new ByAddressTabActivityHandler("BY ADDRESS", new Consumer<StopModel>() {
            @Override
            public void accept(StopModel var1) {
                consumer.accept(var1);
                dismiss();
            }
        }, cursorAdapterSupplier);


        getChildFragmentManager()
                .beginTransaction().add(R.id.stop_picker_container, tabActivityHandlers[1].getFragment()).commit();

        dialogView.findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
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


    public void setConsumer(Consumer<StopModel> consumer) {
        this.consumer = consumer;
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
