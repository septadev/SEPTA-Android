/*
 * RealtimeMenuFragment.java
 * Last modified on 02-04-2014 07:53-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.septa.android.app.BuildConfig;
import org.septa.android.app.R;
import org.septa.android.app.activities.FindNearestLocationActionBarActivity;
import org.septa.android.app.activities.NextToArriveActionBarActivity;
import org.septa.android.app.activities.SystemStatusActionBarActivity;
import org.septa.android.app.activities.TipsActionBarActivity;
import org.septa.android.app.activities.TrainViewActionBarActivity;
import org.septa.android.app.activities.TransitViewActionBarActivity;
import org.septa.android.app.managers.AlertManager;
import org.septa.android.app.managers.SharedPreferencesManager;
import org.septa.android.app.models.servicemodels.AlertModel;
import org.septa.android.app.utilities.PapalVisitUtils;

import java.util.Date;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class RealtimeMenuFragment extends Fragment implements AlertManager.IAlertListener, OnClickListener {
    private static final String TAG = RealtimeMenuFragment.class.getName();

    private static final long MAX_ALERT_AGE = 1000 * 60 * 60;
    private static final String PAPAL_VISIT_DEFAULT_URL = "http://www.septa.org/papalvisitphilly";

    private ImageView mFindNearestLocationImage;
    private ImageView mNextToArriveImage;
    private ImageView mSystemStatusImage;
    private ImageView mTipsImage;
    private ImageView mTrainViewImage;
    private ImageView mTransitViewImage;
    private TextView mPapalVisitMessage;

    private String mPapalVisitUrl;

    public static Boolean fetchedResults = false;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.realtime_menu_fragment, container, false);

        // Setup views
        mPapalVisitMessage = (TextView) view.findViewById(R.id.realtime_menu_papal_message);
        mFindNearestLocationImage = (ImageView) view.findViewById(R.id.realtime_menu_find_nearest_location_image_view);
        mNextToArriveImage = (ImageView) view.findViewById(R.id.realtime_menu_next_to_arrive_image_view);
        mSystemStatusImage = (ImageView) view.findViewById(R.id.realtime_menu_system_status_image_view);
        mTipsImage = (ImageView) view.findViewById(R.id.realtime_menu_tips_image_view);
        mTrainViewImage = (ImageView) view.findViewById(R.id.realtime_menu_trainview_image_view);
        mTransitViewImage = (ImageView) view.findViewById(R.id.realtime_menu_transitview_image_view);

        boolean isPapalVisit = PapalVisitUtils.isPopeVisitingToday();

        // Set image resources
        mNextToArriveImage.setImageResource(isPapalVisit ? R.drawable.realtime_menu_nexttoarrive_selector_disabled : R.drawable.realtime_menu_nexttoarrive_selector);
        mFindNearestLocationImage.setImageResource(isPapalVisit ? R.drawable.realtime_menu_findnearestlocation_selector_disabled : R.drawable.realtime_menu_findnearestlocation_selector);
        mTrainViewImage.setImageResource(isPapalVisit ? R.drawable.realtime_menu_trainview_selector_disabled : R.drawable.realtime_menu_trainview_selector);
        mTransitViewImage.setImageResource(isPapalVisit ? R.drawable.realtime_menu_transitview_selector_disabled : R.drawable.realtime_menu_transitview_selector);

        // Set click listeners
        mSystemStatusImage.setOnClickListener(this);
        mTipsImage.setOnClickListener(this);

        if (isPapalVisit) {

            mPapalVisitMessage.setOnClickListener(this);
        }

        else {

            mFindNearestLocationImage.setOnClickListener(this);
            mNextToArriveImage.setOnClickListener(this);
            mTrainViewImage.setOnClickListener(this);
            mTransitViewImage.setOnClickListener(this);
        }

        // Set visibility
        mPapalVisitMessage.setVisibility(isPapalVisit ? View.VISIBLE : View.GONE);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        AlertManager.getInstance().addListener(this);
        AlertManager.getInstance().fetchGlobalAlert();
    }

    @Override
    public void onPause() {
        AlertManager.getInstance().removeListener(this);
        super.onPause();
    }

    @Override
    public void alertsDidUpdate() {
        Date lastUpdate = SharedPreferencesManager.getInstance().getLastAlertUpdate();
        AlertModel alert = AlertManager.getInstance().getGlobalAlert();
        if (alert != null && !alert.getCurrentMessage().isEmpty() && alert.getLastUpdate() != null && (lastUpdate == null || alert.getLastUpdate().compareTo(lastUpdate) != 0)) {
            Crouton.makeText(getActivity(), alert.getCurrentMessage(), Style.ALERT).show();

            //save the date of the last retrieved alert for comparison on future requests
            SharedPreferencesManager.getInstance().setLastAlertUpdate(alert.getLastUpdate());
        }

    }

    @Override
    public void onClick(View v) {

        Intent intent = null;

        switch (v.getId()) {

            case R.id.realtime_menu_find_nearest_location_image_view:

                intent = new Intent(getActivity(), FindNearestLocationActionBarActivity.class);

                intent.putExtra(getString(R.string.actionbar_titletext_key), getString(R.string.page_title_find_nearest_location));
                intent.putExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key), getString(R.string.page_icon_id_find_nearest_location));

                break;

            case R.id.realtime_menu_next_to_arrive_image_view:

                intent = new Intent(getActivity(), NextToArriveActionBarActivity.class);

                intent.putExtra(getString(R.string.actionbar_titletext_key), getString(R.string.page_title_next_to_arrive));
                intent.putExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key), getString(R.string.page_icon_id_next_to_arrive));

                break;

            case R.id.realtime_menu_system_status_image_view:

                intent = new Intent(getActivity(), SystemStatusActionBarActivity.class);

                intent.putExtra(getString(R.string.actionbar_titletext_key), getString(R.string.page_title_system_status));
                intent.putExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key), getString(R.string.page_icon_id_system_status));

                break;

            case R.id.realtime_menu_tips_image_view:

                intent = new Intent(getActivity(), TipsActionBarActivity.class);

                intent.putExtra(getString(R.string.actionbar_titletext_key), getString(R.string.page_title_tips));
                intent.putExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key), getString(R.string.page_icon_id_tips));

                break;

            case R.id.realtime_menu_trainview_image_view:

                intent = new Intent(getActivity(), TrainViewActionBarActivity.class);

                intent.putExtra(getString(R.string.actionbar_titletext_key), getString(R.string.page_title_train_view));
                intent.putExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key), getString(R.string.page_icon_id_trainview));

                break;

            case R.id.realtime_menu_transitview_image_view:

                intent = new Intent(getActivity(), TransitViewActionBarActivity.class);

                intent.putExtra(getString(R.string.actionbar_titletext_key), getString(R.string.page_title_transit_view));
                intent.putExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key), getString(R.string.page_icon_id_transitview));

                break;

            // Papal Visit Special Message
            case R.id.realtime_menu_papal_message:

                Uri uri = Uri.parse(!TextUtils.isEmpty(mPapalVisitUrl) ? mPapalVisitUrl : PAPAL_VISIT_DEFAULT_URL);
                intent = new Intent(Intent.ACTION_VIEW, uri);

                break;

            default:

                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "onClick: unknown view clicked");
                }

                break;
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}
