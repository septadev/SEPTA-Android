package org.septa.android.app.systemmap;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.septa.android.app.R;
import org.septa.android.app.view.SubsamplingScaleImageView;

/**
 * Created by jkampf on 9/29/17.
 */

public class SystemMapFragment extends Fragment {
    private SubsamplingScaleImageView imageView;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View theView = inflater.inflate(R.layout.system_map_main, container, false);

        imageView = (SubsamplingScaleImageView) theView.findViewById(R.id.imageView);
        imageView.setImageAsset("system-map.png");

        return theView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", getActivity().getTitle().toString());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            String title = savedInstanceState.getString("title");
            if (title != null && getActivity() != null)
                getActivity().setTitle(title);
        }
    }
}
