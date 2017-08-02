package app.android.septa.org.septa_android.nextarrive.railstationpicker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import app.android.septa.org.septa_android.R;
import app.android.septa.org.septa_android.support.BaseTabActivityHandler;

/**
 * Created by jkampf on 7/30/17.
 */

class ByAddressTabActivityHandler extends BaseTabActivityHandler {

    public ByAddressTabActivityHandler(String s, EditText target) {
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
