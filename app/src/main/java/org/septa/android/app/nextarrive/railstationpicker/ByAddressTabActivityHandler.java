package org.septa.android.app.nextarrive.railstationpicker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.septa.android.app.R;
import org.septa.android.app.domain.StopModel;
import org.septa.android.app.support.BaseTabActivityHandler;
import org.septa.android.app.support.Consumer;

/**
 * Created by jkampf on 7/30/17.
 */

class ByAddressTabActivityHandler extends BaseTabActivityHandler {

    public ByAddressTabActivityHandler(String s, Consumer<StopModel> consumer) {
        super(s);
    }

    @Override
    public Fragment getFragment() {
        return ByAddressTabActivityHandler.PlaceholderFragment.newInstance();
    }

    public static class PlaceholderFragment extends Fragment {
        public static ByAddressTabActivityHandler.PlaceholderFragment newInstance() {
            ByAddressTabActivityHandler.PlaceholderFragment fragment = new ByAddressTabActivityHandler.PlaceholderFragment();
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.nhsl_next_to_arrive, container, false);
            return rootView;
        }
    }

}
