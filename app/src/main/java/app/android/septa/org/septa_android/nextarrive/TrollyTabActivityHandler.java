package app.android.septa.org.septa_android.nextarrive;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import septa.org.android.support.BaseTabActivityHandler;
import septa.org.android2.R;

/**
 * Created by jkampf on 7/29/17.
 */

public class TrollyTabActivityHandler extends BaseTabActivityHandler {
    public TrollyTabActivityHandler(String title) {
        super(title);
    }

    @Override
    public Fragment getFragment() {
        return TrollyTabActivityHandler.PlaceholderFragment.newInstance();
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
