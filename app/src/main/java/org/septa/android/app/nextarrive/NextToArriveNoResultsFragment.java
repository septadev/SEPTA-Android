package org.septa.android.app.nextarrive;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;

import org.septa.android.app.R;
import org.septa.android.app.view.TextView;

/**
 * Created by mluansing on 2/23/18.
 */

public class NextToArriveNoResultsFragment extends Fragment {

    private static final String TAG = NextToArriveNoResultsFragment.class.getSimpleName();
    private TextView noResultsSchedulesButton;
    private NoResultsFragmentListener listener;

    public NextToArriveNoResultsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set up listener
        Context context = getActivity();
        if (context instanceof NoResultsFragmentListener) {
            listener = (NoResultsFragmentListener) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.next_to_arrive_results_empty, container, false);

        noResultsSchedulesButton = (TextView) rootView.findViewById(R.id.button_view_schedules);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        noResultsSchedulesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.viewSchedulesClicked();
            }
        });
    }

    public interface NoResultsFragmentListener {
        void viewSchedulesClicked();
    }

}
