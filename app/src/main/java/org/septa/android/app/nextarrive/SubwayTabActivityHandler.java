package org.septa.android.app.nextarrive;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.septa.android.app.R;
import org.septa.android.app.support.BaseTabActivityHandler;

/**
 * Created by jkampf on 7/29/17.
 */

public class SubwayTabActivityHandler extends BaseTabActivityHandler {
    public SubwayTabActivityHandler(String title) {
        super(title);
    }

    @Override
    public Fragment getFragment() {
        return SubwayTabActivityHandler.PlaceholderFragment.newInstance();
    }

    public static class PlaceholderFragment extends Fragment {
        public static PlaceholderFragment newInstance() {
            PlaceholderFragment fragment = new PlaceholderFragment();
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
