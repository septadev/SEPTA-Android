package org.septa.android.app.systemstatus;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.septa.android.app.Constants;
import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.locationpicker.LinePickerFragment;
import org.septa.android.app.services.apiinterfaces.model.AlertDetail;
import org.septa.android.app.support.BaseTabActivityHandler;
import org.septa.android.app.support.Consumer;
import org.septa.android.app.support.CursorAdapterSupplier;
import org.w3c.dom.Text;

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
            return SystemStatusPickerFragement.getInstance(transitType, routeCursorAdapterSupplier);
        else return SystemStatusPickerFragement.getInstance(transitType, routeDirectionModel);
    }


    public static class SystemStatusPickerFragement extends Fragment {
        TransitType transitType;
        CursorAdapterSupplier<RouteDirectionModel> routeCursorAdapterSupplier;

        TextView globalAlertTitle;
        View globalAlertScrollview;
        View globalAlertView;
        View queryButton;
        TextView lineText;
        boolean globalAlertsExpanded = false;
        RouteDirectionModel routeDirectionModel;


        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);

            View fragmentView = inflater.inflate(R.layout.system_status_line_picker, null);

            TextView pickerHeaderText = (TextView) fragmentView.findViewById(R.id.picker_header_text);
            pickerHeaderText.setText(transitType.getString("system_status_picker_title", getContext()));

            AlertDetail globalAlertDetail = GlobalSystemStatus.getGlobalAlertDetails();

            if (globalAlertDetail != null &&
                    globalAlertDetail.getAlerts().get(0).getMessage() != null &&
                    !globalAlertDetail.getAlerts().get(0).getMessage().equals("")) {
                globalAlertScrollview = fragmentView.findViewById(R.id.global_alert_scrollview);
                globalAlertView = fragmentView.findViewById(R.id.global_alert_view);
                globalAlertView.setVisibility(View.VISIBLE);

                StringBuilder alertText = new StringBuilder();

                for (AlertDetail.Alerts alert : globalAlertDetail.getAlerts()) {
                    alertText.append("<b>ADVISORIES<b><p>").append(alert.getAdvisoryMessage());
                }

                org.septa.android.app.view.TextView globalAlertText = (org.septa.android.app.view.TextView) fragmentView.findViewById(R.id.global_alert_text);
                globalAlertText.setHtml(alertText.toString());
            }

            queryButton = fragmentView.findViewById(R.id.view_status_button);

            queryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), SystemStatusResultsActivity.class);
                    intent.putExtra(Constants.LINE_ID, routeDirectionModel);
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

                        LinePickerFragment newFragment = LinePickerFragment.newInstance(routeCursorAdapterSupplier, transitType, new Consumer<RouteDirectionModel>() {
                            @Override
                            public void accept(RouteDirectionModel var1) {
                                routeDirectionModel = var1;
                                lineText.setText(var1.getRouteLongName());

                                int color;
                                try {
                                    color = ContextCompat.getColor(getContext(), transitType.getLineColor(var1.getRouteId(), getContext()));
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
                        });

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

        public static SystemStatusPickerFragement getInstance(TransitType transitType, CursorAdapterSupplier<RouteDirectionModel> routeCursorAdapterSupplier) {
            SystemStatusPickerFragement fragement = new SystemStatusPickerFragement();
            fragement.transitType = transitType;
            fragement.routeCursorAdapterSupplier = routeCursorAdapterSupplier;

            return fragement;
        }

        public static SystemStatusPickerFragement getInstance(TransitType transitType, RouteDirectionModel routeDirectionModel) {
            SystemStatusPickerFragement fragement = new SystemStatusPickerFragement();
            fragement.transitType = transitType;
            fragement.routeDirectionModel = routeDirectionModel;
            return fragement;
        }
    }
}
