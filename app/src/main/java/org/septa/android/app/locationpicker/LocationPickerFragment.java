package org.septa.android.app.locationpicker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.support.CursorAdapterSupplier;
import org.septa.android.app.support.TabActivityHandler;

public class LocationPickerFragment extends DialogFragment implements StopPickerTabListener {
    private static final String TAG = LocationPickerFragment.class.getSimpleName();
    public static final int SUCCESS = 0;
    public static final String STOP_MODEL = "stopModel";
    public static final int STOP_MODEL_REQUEST = 1;
    private static final String IS_LINE_AWARE = "IS_LINE_AWARE";

    TabActivityHandler tabActivityHandlers[];

    private TextView searchByStationTab;
    private TextView searchByAddressTab;

    private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;
    private LocationPickerCallBack locationPickerCallBack;

    private int selected_index = 0;
    private Fragment currentFragment;

    private boolean isLineAware = false;

    public static LocationPickerFragment newInstance(CursorAdapterSupplier<StopModel> cursorAdapterSupplier, boolean lineAware) {
        LocationPickerFragment fragment = new LocationPickerFragment();

        Bundle args = new Bundle();
        args.putSerializable("cursorAdapterSupplier", cursorAdapterSupplier);
        args.putBoolean(IS_LINE_AWARE, lineAware);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onResume() {
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Integer newIndex = (Integer) savedInstanceState.getSerializable("selected_index");
            if (newIndex != null) {
                selected_index = newIndex;
            }
        }

        restoreArgs();

        // remove unnecessary whitespace
        if (getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        if (getActivity() == null) {
            return null;
        }

        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.by_station, null);

        tabActivityHandlers = new TabActivityHandler[2];
        tabActivityHandlers[0] = new ByStopTabActivityHandler(LocationPickerFragment.this,"BY STATION", cursorAdapterSupplier, isLineAware);
        tabActivityHandlers[1] = new ByAddressTabActivityHandler(LocationPickerFragment.this,"BY ADDRESS", cursorAdapterSupplier);

        searchByStationTab = dialogView.findViewById(R.id.search_by_station_tab);
        searchByAddressTab = dialogView.findViewById(R.id.search_by_address_tab);

        if (selected_index == 0) {
            setActive(searchByStationTab, searchByAddressTab);
        } else {
            setActive(searchByAddressTab, searchByStationTab);
        }
        currentFragment = tabActivityHandlers[selected_index].getFragment();
        getChildFragmentManager().beginTransaction().replace(R.id.stop_picker_container, currentFragment).commit();

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
                currentFragment = tabActivityHandlers[0].getFragment();
                selected_index = 0;
                getChildFragmentManager().beginTransaction().replace(R.id.stop_picker_container, currentFragment).commit();
            }
        });

        searchByAddressTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setActive(searchByAddressTab, searchByStationTab);
                currentFragment = tabActivityHandlers[1].getFragment();
                selected_index = 1;
                getChildFragmentManager().beginTransaction().replace(R.id.stop_picker_container, currentFragment).commit();

            }
        });

        return dialogView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if ((context instanceof LocationPickerCallBack)) {
            locationPickerCallBack = (LocationPickerCallBack) context;
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (currentFragment != null) {
            getChildFragmentManager().beginTransaction().remove(currentFragment).commit();
        }

        outState.putSerializable("selected_index", selected_index);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // pass the selected stop back to the picker fragment
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
        }
    }

    @Override
    public void onStopSelected(Intent intent) {
        onActivityResult(STOP_MODEL_REQUEST, SUCCESS, intent);
    }

    private void restoreArgs() {
        cursorAdapterSupplier = (CursorAdapterSupplier<StopModel>) getArguments().getSerializable("cursorAdapterSupplier");
        isLineAware = getArguments().getBoolean(IS_LINE_AWARE);
    }

    private void setActive(TextView active, TextView inactive) {
        active.setBackgroundResource(R.drawable.bg_stop_picker_active);
        inactive.setBackgroundResource(R.drawable.bg_stop_picker_inactive);
        active.setTextColor(ContextCompat.getColor(getContext(), R.color.find_station_tab_active_text));
        inactive.setTextColor(ContextCompat.getColor(getContext(), R.color.find_station_tab_inactive_text));
    }

}
