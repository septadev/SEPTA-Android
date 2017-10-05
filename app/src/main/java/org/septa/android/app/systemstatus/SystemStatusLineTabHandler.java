package org.septa.android.app.systemstatus;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.method.LinkMovementMethod;
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
import org.septa.android.app.support.BaseTabActivityHandler;
import org.septa.android.app.support.CursorAdapterSupplier;
import org.septa.android.app.support.GeneralUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by jkampf on 9/11/17.
 */

public class SystemStatusLineTabHandler extends BaseTabActivityHandler {
    public static final String TAG = SystemStatusLineTabHandler.class.getSimpleName();
    TransitType transitType;
    CursorAdapterSupplier<RouteDirectionModel> routeCursorAdapterSupplier;
    RouteDirectionModel routeDirectionModel;


    public SystemStatusLineTabHandler(String title, TransitType transitType, CursorAdapterSupplier<RouteDirectionModel> routeCursorAdapterSupplier) {
        super(title, transitType.getTabInactiveImageResource(), transitType.getTabActiveImageResource());
        this.transitType = transitType;
        this.routeCursorAdapterSupplier = routeCursorAdapterSupplier;
    }

    public SystemStatusLineTabHandler(String title, TransitType transitType, RouteDirectionModel routeDirectionModel) {
        super(title, transitType.getTabInactiveImageResource(), transitType.getTabActiveImageResource());
        this.transitType = transitType;
        this.routeDirectionModel = routeDirectionModel;
    }

    @Override
    public Fragment getFragment() {
        if (routeCursorAdapterSupplier != null)
            return SystemStatusPickerFragment.getInstance(transitType, routeCursorAdapterSupplier);
        else return SystemStatusPickerFragment.getInstance(transitType, routeDirectionModel);
    }


    public static class SystemStatusPickerFragment extends Fragment implements LinePickerCallBack {
        private static final int LINE_PICKER_ID = 1;
        TransitType transitType;
        CursorAdapterSupplier<RouteDirectionModel> routeCursorAdapterSupplier;

        TextView globalAlertTitle;
        View globalAlertScrollview;
        View globalAlertView;
        View queryButton;
        TextView lineText;
        boolean globalAlertsExpanded = false;
        RouteDirectionModel routeDirectionModel;
        org.septa.android.app.view.TextView globalAlertText;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            restoreArgs();

            View fragmentView = inflater.inflate(R.layout.system_status_line_picker, null);

            TextView pickerHeaderText = (TextView) fragmentView.findViewById(R.id.picker_header_text);
            pickerHeaderText.setText(transitType.getString("system_status_picker_title", getContext()));

            globalAlertScrollview = fragmentView.findViewById(R.id.global_alert_scrollview);
            globalAlertView = fragmentView.findViewById(R.id.global_alert_view);
            globalAlertText = (org.septa.android.app.view.TextView) fragmentView.findViewById(R.id.global_alert_text);
            globalAlertText.setMovementMethod(LinkMovementMethod.getInstance());


            SeptaServiceFactory.getAlertDetailsService().getAlertDetails("generic").enqueue(new GlobalStatusCallBack());

            queryButton = fragmentView.findViewById(R.id.view_status_button);

            queryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), SystemStatusResultsActivity.class);
                    intent.putExtra(Constants.ROUTE_DIRECTION_MODEL, routeDirectionModel);
                    intent.putExtra(Constants.TRANSIT_TYPE, transitType);

                    startActivity(intent);
                }
            });

            lineText = (TextView) fragmentView.findViewById(R.id.line_text);

            if (routeCursorAdapterSupplier == null) {
                fragmentView.findViewById(R.id.select_line_label).setVisibility(View.GONE);
                lineText.setVisibility(View.GONE);
                queryButton.setAlpha(1);
                queryButton.setClickable(true);
            } else {
                lineText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FragmentTransaction ft = getChildFragmentManager().beginTransaction();

                        LinePickerFragment newFragment = LinePickerFragment.newInstance(routeCursorAdapterSupplier, transitType);
                        newFragment.setTargetFragment(SystemStatusPickerFragment.this, LINE_PICKER_ID);
                        newFragment.show(ft, "line_picker");
                    }
                });
            }

            globalAlertTitle = (TextView) fragmentView.findViewById(R.id.global_alert_title);
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

            return fragmentView;

        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            //do what ever you want here, and get the result from intent like below
            if (requestCode == LINE_PICKER_ID && resultCode == LinePickerFragment.SUCCESS) {
                RouteDirectionModel var1 = (RouteDirectionModel) data.getSerializableExtra(LinePickerFragment.ROUTE_DIRECTION_MODEL);
                if (var1 != null) {
                    setRoute(var1);
                }
            }
        }

        @Override
        public void setRoute(RouteDirectionModel routeDirectionModel) {
            this.routeDirectionModel = routeDirectionModel;
            lineText.setText(routeDirectionModel.getRouteLongName());

            int color;
            try {
                color = ContextCompat.getColor(getContext(), transitType.getLineColor(routeDirectionModel.getRouteId(), getContext()));
            } catch (Exception e) {
                color = ContextCompat.getColor(getContext(), R.color.default_line_color);
            }

            Drawable[] drawables = lineText.getCompoundDrawables();
            Drawable bullet = ContextCompat.getDrawable(getContext(), R.drawable.shape_line_marker);
            bullet.setColorFilter(color, PorterDuff.Mode.SRC);

            lineText.setCompoundDrawablesWithIntrinsicBounds(bullet, drawables[1],
                    drawables[2], drawables[3]);
            queryButton.setAlpha(1);
            queryButton.setClickable(true);
        }

        private class GlobalStatusCallBack implements Callback<AlertDetail> {
            @Override
            public void onResponse(Call<AlertDetail> call, Response<AlertDetail> response) {
                AlertDetail globalAlertDetail = response.body();

                boolean found = false;
                StringBuilder alertText = new StringBuilder();

                if (globalAlertDetail != null &&
                        globalAlertDetail.getAlerts().get(0).getMessage() != null &&
                        !globalAlertDetail.getAlerts().get(0).getMessage().equals("")) {
                    globalAlertView.setVisibility(View.VISIBLE);

                    for (AlertDetail.Detail alert : globalAlertDetail.getAlerts()) {
                        if (!"".equals(alert.getAdvisoryMessage())) {
                            alertText.append("<b>ADVISORIES<b><p>").append(alert.getAdvisoryMessage());
                            found = true;
                        }

                        if (!"".equals(alert.getMessage())) {
                            alertText.append("<b>Alert<b><p>").append(alert.getMessage());
                            found = true;
                        }
                    }
                }

                if (found) {
                    globalAlertText.setHtml(GeneralUtils.updateUrls(alertText.toString()));
                } else {
                    globalAlertView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<AlertDetail> call, Throwable t) {

            }
        }

        public static SystemStatusPickerFragment getInstance(TransitType transitType, CursorAdapterSupplier<RouteDirectionModel> routeCursorAdapterSupplier) {
            SystemStatusPickerFragment fragment = new SystemStatusPickerFragment();
            Bundle args = new Bundle();

            args.putSerializable("transitType", transitType);
            args.putSerializable("routeCursorAdapterSupplier", routeCursorAdapterSupplier);

            fragment.setArguments(args);

            return fragment;
        }

        public static SystemStatusPickerFragment getInstance(TransitType transitType, RouteDirectionModel routeDirectionModel) {
            SystemStatusPickerFragment fragment = new SystemStatusPickerFragment();
            Bundle args = new Bundle();
            args.putSerializable("transitType", transitType);
            args.putSerializable("routeDirectionModel", routeDirectionModel);

            fragment.setArguments(args);

            return fragment;
        }

        private void restoreArgs() {
            transitType = (TransitType) getArguments().getSerializable("transitType");
            routeDirectionModel = (RouteDirectionModel) getArguments().getSerializable("routeDirectionModel");
            routeCursorAdapterSupplier = (CursorAdapterSupplier<RouteDirectionModel>) getArguments().getSerializable("routeCursorAdapterSupplier");

        }
    }

}
