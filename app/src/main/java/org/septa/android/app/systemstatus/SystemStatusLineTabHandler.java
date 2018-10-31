package org.septa.android.app.systemstatus;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.locationpicker.LinePickerCallBack;
import org.septa.android.app.locationpicker.LinePickerFragment;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.AlertDetail;
import org.septa.android.app.support.AnalyticsManager;
import org.septa.android.app.support.BaseTabActivityHandler;
import org.septa.android.app.support.CrashlyticsManager;
import org.septa.android.app.support.CursorAdapterSupplier;
import org.septa.android.app.support.GeneralUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SystemStatusLineTabHandler extends BaseTabActivityHandler {

    public static final String TAG = SystemStatusLineTabHandler.class.getSimpleName();

    private TransitType transitType;
    private CursorAdapterSupplier<RouteDirectionModel> routeCursorAdapterSupplier;
    private RouteDirectionModel routeDirectionModel;

    SystemStatusLineTabHandler(String title, TransitType transitType, CursorAdapterSupplier<RouteDirectionModel> routeCursorAdapterSupplier) {
        super(title, transitType.getTabInactiveImageResource(), transitType.getTabActiveImageResource());
        this.transitType = transitType;
        this.routeCursorAdapterSupplier = routeCursorAdapterSupplier;
    }

    SystemStatusLineTabHandler(String title, TransitType transitType, RouteDirectionModel routeDirectionModel) {
        super(title, transitType.getTabInactiveImageResource(), transitType.getTabActiveImageResource());
        this.transitType = transitType;
        this.routeDirectionModel = routeDirectionModel;
    }

    @Override
    public Fragment getFragment() {
        if (routeCursorAdapterSupplier != null) {
            return SystemStatusPickerFragment.newInstance(transitType, routeCursorAdapterSupplier);
        } else {
            return SystemStatusPickerFragment.newInstance(transitType, routeDirectionModel);
        }
    }

    public static class SystemStatusPickerFragment extends Fragment implements LinePickerCallBack {
        private static final int LINE_PICKER_ID = 1;
        private TransitType transitType;
        private CursorAdapterSupplier<RouteDirectionModel> routeCursorAdapterSupplier;

        private static final String GLOBAL_ALERT_ROUTE_ID = "generic", MOBILE_ALERT_ROUTE_ID = "APP";

        private TextView globalAlertTitle, mobileAlertTitle;
        private View globalAlertScrollview, mobileAlertScrollview;
        private View globalAlertView, mobileAlertView;
        private View queryButton;
        private TextView lineText;
        private boolean globalAlertsExpanded = false, mobileAlertsExpanded = false;
        private RouteDirectionModel routeDirectionModel;
        private org.septa.android.app.view.TextView globalAlertText, mobileAlertText;

        public static SystemStatusPickerFragment newInstance(TransitType transitType, CursorAdapterSupplier<RouteDirectionModel> routeCursorAdapterSupplier) {
            SystemStatusPickerFragment fragment = new SystemStatusPickerFragment();

            Bundle args = new Bundle();
            args.putSerializable("transitType", transitType);
            args.putSerializable("routeCursorAdapterSupplier", routeCursorAdapterSupplier);
            fragment.setArguments(args);

            return fragment;
        }

        public static SystemStatusPickerFragment newInstance(TransitType transitType, RouteDirectionModel routeDirectionModel) {
            SystemStatusPickerFragment fragment = new SystemStatusPickerFragment();

            Bundle args = new Bundle();
            args.putSerializable("transitType", transitType);
            if (routeDirectionModel != null) {
                args.putSerializable("routeDirectionModel", routeDirectionModel);
            }
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            restoreArgs();

            View fragmentView = inflater.inflate(R.layout.fragment_system_status, null);

            TextView pickerHeaderText = fragmentView.findViewById(R.id.picker_header_text);
            pickerHeaderText.setText(transitType.getString("system_status_picker_title", getContext()));

            // set global alerts up
            globalAlertScrollview = fragmentView.findViewById(R.id.global_alert_scrollview);
            globalAlertView = fragmentView.findViewById(R.id.global_alert_view);
            globalAlertText = fragmentView.findViewById(R.id.global_alert_text);
            globalAlertText.setMovementMethod(LinkMovementMethod.getInstance());

            // set mobile app alerts up
            mobileAlertScrollview = fragmentView.findViewById(R.id.mobile_alert_scrollview);
            mobileAlertView = fragmentView.findViewById(R.id.mobile_alert_view);
            mobileAlertText = fragmentView.findViewById(R.id.mobile_alert_text);
            mobileAlertText.setMovementMethod(LinkMovementMethod.getInstance());

            queryButton = fragmentView.findViewById(R.id.view_status_button);

            queryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getActivity() == null) {
                        return;
                    }
                    Intent intent = new Intent(getActivity(), SystemStatusResultsActivity.class);
                    intent.putExtra(Constants.ROUTE_DIRECTION_MODEL, routeDirectionModel);
                    intent.putExtra(Constants.TRANSIT_TYPE, transitType);

                    AnalyticsManager.logContentViewEvent(TAG, AnalyticsManager.CONTENT_VIEW_EVENT_SYSTEM_STATUS_FROM_PICKER, AnalyticsManager.CONTENT_ID_SYSTEM_STATUS, null);

                    startActivityForResult(intent, Constants.SYSTEM_STATUS_REQUEST);
                }
            });

            queryButton.setClickable(false);

            lineText = fragmentView.findViewById(R.id.line_text);

            if (routeCursorAdapterSupplier == null) {
                fragmentView.findViewById(R.id.select_line_label).setVisibility(View.GONE);
                lineText.setVisibility(View.GONE);
                queryButton.setAlpha(1);
                queryButton.setClickable(true);
            } else {
                lineText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();

                        LinePickerFragment newFragment = LinePickerFragment.newInstance(routeCursorAdapterSupplier, transitType);
                        newFragment.setTargetFragment(SystemStatusPickerFragment.this, LINE_PICKER_ID);
                        newFragment.show(ft, "line_picker");
                    }
                });
            }

            // global alert expansion handling
            globalAlertTitle = fragmentView.findViewById(R.id.global_alert_title);
            globalAlertTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!globalAlertsExpanded) {
                        Drawable[] drawables = globalAlertTitle.getCompoundDrawables();
                        globalAlertTitle.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1],
                                ContextCompat.getDrawable(getContext(), R.drawable.alert_toggle_open), drawables[3]);
                        globalAlertScrollview.setVisibility(View.VISIBLE);
                        globalAlertsExpanded = true;
                    } else {
                        Drawable[] drawables = globalAlertTitle.getCompoundDrawables();
                        globalAlertTitle.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1],
                                ContextCompat.getDrawable(getContext(), R.drawable.alert_toggle_closed), drawables[3]);
                        globalAlertScrollview.setVisibility(View.GONE);
                        globalAlertsExpanded = false;
                    }
                }
            });

            // mobile alert expansion handling
            mobileAlertTitle = fragmentView.findViewById(R.id.mobile_alert_title);
            mobileAlertTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mobileAlertsExpanded) {
                        Drawable[] drawables = mobileAlertTitle.getCompoundDrawables();
                        mobileAlertTitle.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1],
                                ContextCompat.getDrawable(getContext(), R.drawable.alert_toggle_open), drawables[3]);
                        mobileAlertScrollview.setVisibility(View.VISIBLE);
                        mobileAlertsExpanded = true;
                    } else {
                        Drawable[] drawables = mobileAlertTitle.getCompoundDrawables();
                        mobileAlertTitle.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1],
                                ContextCompat.getDrawable(getContext(), R.drawable.alert_toggle_closed), drawables[3]);
                        mobileAlertScrollview.setVisibility(View.GONE);
                        mobileAlertsExpanded = false;
                    }
                }
            });

            restoreState(savedInstanceState);

            return fragmentView;
        }

        @Override
        public void onResume() {
            super.onResume();

            // must call alerts onResume so page reloads
            SeptaServiceFactory.getAlertDetailsService().getAlertDetails(MOBILE_ALERT_ROUTE_ID).enqueue(new MobileStatusCallBack());
            SeptaServiceFactory.getAlertDetailsService().getAlertDetails(GLOBAL_ALERT_ROUTE_ID).enqueue(new GlobalStatusCallBack());
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putSerializable("routeDirectionModel", routeDirectionModel);
        }

        @Override
        public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
            super.onViewStateRestored(savedInstanceState);
            restoreState(savedInstanceState);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            // pass selected route back to the system status picker fragment
            if (requestCode == LINE_PICKER_ID && resultCode == LinePickerFragment.SUCCESS) {
                RouteDirectionModel var1 = (RouteDirectionModel) data.getSerializableExtra(LinePickerFragment.ROUTE_DIRECTION_MODEL);
                if (var1 != null) {
                    setRoute(var1);
                }
            }
        }

        @Override
        public void setRoute(RouteDirectionModel routeDirectionModel) {
            Context context = getContext();

            this.routeDirectionModel = routeDirectionModel;
            lineText.setText(getString(R.string.line_picker_selection, routeDirectionModel.getRouteId(), routeDirectionModel.getRouteLongName()));

            if (context != null) {
                int color;
                try {
                    color = ContextCompat.getColor(context, transitType.getLineColor(routeDirectionModel.getRouteId(), getContext()));
                } catch (Exception e) {
                    CrashlyticsManager.log(Log.ERROR, TAG, "Could not retrieve route color for " + routeDirectionModel.getRouteId());
                    CrashlyticsManager.logException(TAG, e);

                    color = ContextCompat.getColor(context, R.color.default_line_color);
                }

                Drawable[] drawables = lineText.getCompoundDrawables();
                Drawable bullet = ContextCompat.getDrawable(context, R.drawable.shape_line_marker);
                bullet.setColorFilter(color, PorterDuff.Mode.SRC);

                lineText.setCompoundDrawablesWithIntrinsicBounds(bullet, drawables[1], drawables[2], drawables[3]);
            }

            queryButton.setAlpha(1);
            queryButton.setClickable(true);

        }

        private class GlobalStatusCallBack implements Callback<AlertDetail> {
            @Override
            public void onResponse(Call<AlertDetail> call, Response<AlertDetail> response) {
                AlertDetail globalAlertDetail = response.body();

                boolean found = false;
                StringBuilder alertText = new StringBuilder();

                if (globalAlertDetail != null && GLOBAL_ALERT_ROUTE_ID.equals(globalAlertDetail.getRoute())
                        && globalAlertDetail.getAlerts().size() > 0 &&
                        globalAlertDetail.getAlerts().get(0).getMessage() != null &&
                        !globalAlertDetail.getAlerts().get(0).getMessage().trim().isEmpty()) {
                    globalAlertView.setVisibility(View.VISIBLE);

                    for (AlertDetail.Detail alert : globalAlertDetail.getAlerts()) {
                        if (!alert.getAdvisoryMessage().isEmpty()) {
                            alertText.append("<b>ADVISORIES<b><p>").append(alert.getAdvisoryMessage());
                            found = true;
                        }

                        if (!alert.getMessage().isEmpty()) {
                            alertText.append(alert.getMessage());
                            found = true;
                        }
                    }
                }

                if (found) {
                    globalAlertText.setHtml(GeneralUtils.updateUrls(alertText.toString()));
                    Linkify.addLinks(globalAlertText, Linkify.WEB_URLS);
                } else {
                    globalAlertView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<AlertDetail> call, Throwable t) {
                Log.e(TAG, "Error with calling global application alerts.");
            }
        }

        private class MobileStatusCallBack implements Callback<AlertDetail> {
            @Override
            public void onResponse(Call<AlertDetail> call, Response<AlertDetail> response) {
                AlertDetail mobileAlertDetail = response.body();

                boolean found = false;
                StringBuilder alertText = new StringBuilder();

                if (mobileAlertDetail != null && MOBILE_ALERT_ROUTE_ID.equals(mobileAlertDetail.getRoute())
                        && mobileAlertDetail.getAlerts().size() > 0 &&
                        mobileAlertDetail.getAlerts().get(0).getMessage() != null &&
                        !mobileAlertDetail.getAlerts().get(0).getMessage().trim().isEmpty()) {
                    mobileAlertView.setVisibility(View.VISIBLE);

                    for (AlertDetail.Detail alert : mobileAlertDetail.getAlerts()) {
                        if (!alert.getAdvisoryMessage().trim().isEmpty()) {
                            alertText.append(alert.getAdvisoryMessage());
                            found = true;
                        }

                        if (!alert.getMessage().trim().isEmpty()) {
                            alertText.append(alert.getMessage());
                            found = true;
                        }
                    }
                }

                if (found) {
                    mobileAlertText.setHtml(GeneralUtils.updateUrls(alertText.toString()));
                    Linkify.addLinks(mobileAlertText, Linkify.WEB_URLS);
                } else {
                    mobileAlertView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<AlertDetail> call, Throwable t) {
                Log.e(TAG, "Error with calling mobile application alerts.");
            }
        }

        private void restoreState(Bundle outState) {
            if (outState == null) {
                return;
            }

            RouteDirectionModel rdm = (RouteDirectionModel) outState.getSerializable("routeDirectionModel");
            if (rdm != null) {
                setRoute(rdm);
            }

        }

        private void restoreArgs() {
            transitType = (TransitType) getArguments().getSerializable("transitType");
            if (getArguments().containsKey("routeDirectionModel")) {
                routeDirectionModel = (RouteDirectionModel) getArguments().getSerializable("routeDirectionModel");
            }

            routeCursorAdapterSupplier = (CursorAdapterSupplier<RouteDirectionModel>) getArguments().getSerializable("routeCursorAdapterSupplier");

        }
    }

}
