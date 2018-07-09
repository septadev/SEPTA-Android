package org.septa.android.app.favorites;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.septa.android.app.nextarrive.NextArrivalModelResponseParser;
import org.septa.android.app.nextarrive.NextToArriveTripView;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Alert;
import org.septa.android.app.services.apiinterfaces.model.Alerts;
import org.septa.android.app.services.apiinterfaces.model.Favorite;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalFavorite;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalModelResponse;
import org.septa.android.app.services.apiinterfaces.model.TransitViewFavorite;
import org.septa.android.app.systemstatus.SystemStatusState;
import org.septa.android.app.transitview.TransitViewUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.septa.android.app.transitview.TransitViewUtils.isTrolley;

class FavoriteItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = FavoriteItemAdapter.class.getSimpleName();

    private Context context;
    private FavoriteItemListener mListener;
    private List<FavoriteState> mFavoriteStateList;

    private static final int NTA_FAVORITE = 0, TRANSITVIEW_FAVORITE = 1;

    FavoriteItemAdapter(Context context, List<FavoriteState> list, FavoriteItemListener favoriteItemListener) {
        this.context = context;
        this.mFavoriteStateList = list;
        mListener = favoriteItemListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder holder;
        View view;

        switch (viewType) {
            case NTA_FAVORITE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite_nta, parent, false);
                holder = new NTAFavoriteViewHolder(view);
                break;

            case TRANSITVIEW_FAVORITE:
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite_transitview, parent, false);
                holder = new TransitViewFavoriteViewHolder(view);
                break;
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        final FavoriteState favoriteState = mFavoriteStateList.get(position);
        String favoriteKey = favoriteState.getFavoriteKey();

        Favorite tempFavorite = SeptaServiceFactory.getFavoritesService().getFavoriteByKey(context, favoriteKey);

        if (tempFavorite == null) {
            Log.e(TAG, "Favorite not found");
            // TODO: hide row

        } else if (tempFavorite instanceof NextArrivalFavorite) {
            final NextArrivalFavorite nextArrivalFavorite = (NextArrivalFavorite) tempFavorite;
            final NTAFavoriteViewHolder ntaFavoriteViewHolder = (NTAFavoriteViewHolder) holder;

            // nextArrivalFavorite name
            ntaFavoriteViewHolder.favoriteName.setText(nextArrivalFavorite.getName());

            // transit type icon on left
            Drawable drawables[] = ntaFavoriteViewHolder.favoriteName.getCompoundDrawables();
            ntaFavoriteViewHolder.favoriteName.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context,
                    nextArrivalFavorite.getTransitType().getTabActiveImageResource()),
                    drawables[1], drawables[2], drawables[3]);

            // progress view
            ntaFavoriteViewHolder.progressView.setVisibility(View.VISIBLE);

            // NTA results container
            ntaFavoriteViewHolder.tripView.setMaxResults(3);
            ntaFavoriteViewHolder.tripView.setTransitType(nextArrivalFavorite.getTransitType());
            ntaFavoriteViewHolder.tripView.setStart(nextArrivalFavorite.getStart());
            ntaFavoriteViewHolder.tripView.setDestination(nextArrivalFavorite.getDestination());
            ntaFavoriteViewHolder.tripView.setRouteDirectionModel(nextArrivalFavorite.getRouteDirectionModel());
            ntaFavoriteViewHolder.resultsContainer.removeAllViews();
            ntaFavoriteViewHolder.resultsContainer.addView(ntaFavoriteViewHolder.tripView);

            // refresh nextArrivalFavorite results
            refreshNTAFavoriteView(nextArrivalFavorite, ntaFavoriteViewHolder.favoriteHeader, ntaFavoriteViewHolder.tripView, ntaFavoriteViewHolder.progressView, ntaFavoriteViewHolder.expandCollapseButton, ntaFavoriteViewHolder.noResultsMsg);

            // initialize expanded state of nextArrivalFavorite
            if (favoriteState.isExpanded()) {
                expandFavorite(ntaFavoriteViewHolder.resultsContainer, ntaFavoriteViewHolder.expandCollapseButton);
            } else {
                collapseFavorite(ntaFavoriteViewHolder.resultsContainer, ntaFavoriteViewHolder.expandCollapseButton);
            }

            // toggle expand / collapse button
            ntaFavoriteViewHolder.favoriteHeader.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // collapse nextArrivalFavorite if already expanded
                    // save expanded / collapsed state to service
                    if (favoriteState.isExpanded()) {
                        collapseFavorite(ntaFavoriteViewHolder.resultsContainer, ntaFavoriteViewHolder.expandCollapseButton);
                        favoriteState.setExpanded(false);
                        SeptaServiceFactory.getFavoritesService().modifyFavoriteState(context, holder.getAdapterPosition(), false);
                    } else {
                        expandFavorite(ntaFavoriteViewHolder.resultsContainer, ntaFavoriteViewHolder.expandCollapseButton);
                        favoriteState.setExpanded(true);
                        SeptaServiceFactory.getFavoritesService().modifyFavoriteState(context, holder.getAdapterPosition(), true);
                    }
                }
            });

            // clicking on no results message navigates to prepopulated schedule selection picker
            ntaFavoriteViewHolder.noResultsMsg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.goToSchedulesForTarget(nextArrivalFavorite);
                }
            });

            // clicking on results opens NTA Results
            ntaFavoriteViewHolder.resultsContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.goToNextToArrive(nextArrivalFavorite);
                }
            });

        } else if (tempFavorite instanceof TransitViewFavorite) {
            final TransitViewFavorite transitViewFavorite = (TransitViewFavorite) tempFavorite;
            final TransitViewFavoriteViewHolder transitViewFavoriteViewHolder = (TransitViewFavoriteViewHolder) holder;

            transitViewFavoriteViewHolder.favoriteName.setText(transitViewFavorite.getName());

            transitViewFavoriteViewHolder.viewResults.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.goToTransitView(transitViewFavorite);
                }
            });

            // refresh alerts
            SeptaServiceFactory.getAlertsService().getAlerts().enqueue(new Callback<Alerts>() {
                @Override
                public void onResponse(Call<Alerts> call, Response<Alerts> response) {
                    SystemStatusState.update(response.body());

                    // show alert changes in favorite
                    refreshTransitViewFavorite(transitViewFavorite, transitViewFavoriteViewHolder);
                }

                @Override
                public void onFailure(Call<Alerts> call, Throwable t) {
                    t.printStackTrace();

                    // hide alerts when no connection
                    transitViewFavoriteViewHolder.advisoryIcon.setVisibility(View.GONE);
                    transitViewFavoriteViewHolder.alertIcon.setVisibility(View.GONE);
                    transitViewFavoriteViewHolder.detourIcon.setVisibility(View.GONE);
                    transitViewFavoriteViewHolder.weatherIcon.setVisibility(View.GONE);
                }
            });

        } else {
            Log.e(TAG, "Invalid Favorite class Type in onBindViewHolder");
        }

    }

    @Override
    public int getItemCount() {
        return mFavoriteStateList.size();
    }

    @Override
    public int getItemViewType(int position) {
        String favoriteKey = mFavoriteStateList.get(position).getFavoriteKey();

        // determine if favorite is transitview or NTA
        return TransitViewUtils.isATransitViewFavorite(favoriteKey) ? TRANSITVIEW_FAVORITE : NTA_FAVORITE;
    }

    private void updateViewWhenResultsFound(LinearLayout favoriteHeader, ImageButton expandCollapseButton, LinearLayout noResultsMsg) {
        favoriteHeader.setClickable(true);
        noResultsMsg.setVisibility(View.GONE);
        expandCollapseButton.setVisibility(View.VISIBLE);
    }

    private void updateViewWhenNoResultsFound(LinearLayout favoriteHeader, ImageButton expandCollapseButton, LinearLayout noResultsMsg) {
        favoriteHeader.setClickable(false);
        expandCollapseButton.setVisibility(View.GONE);
        noResultsMsg.setVisibility(View.VISIBLE);
    }

    private void collapseFavorite(ViewGroup favoriteResults, ImageButton expandCollapseButton) {
        // collapse favorite row in UI
        expandCollapseButton.setImageResource(R.drawable.ic_expand);
        favoriteResults.setVisibility(View.GONE);
    }

    private void expandFavorite(ViewGroup favoriteResults, ImageButton expandCollapseButton) {
        // expand favorite row in UI
        expandCollapseButton.setImageResource(R.drawable.ic_collapse);
        favoriteResults.setVisibility(View.VISIBLE);
    }

    public void refreshFavorites(List<FavoriteState> favoriteStateList) {
        this.mFavoriteStateList.clear();
        this.mFavoriteStateList.addAll(favoriteStateList);

        // update UI for favorites
        notifyDataSetChanged();
    }

    private void refreshNTAFavoriteView(final NextArrivalFavorite nextArrivalFavorite, final LinearLayout favoriteHeader, final NextToArriveTripView tripView, final View progressView, final ImageButton expandCollapseButton, final LinearLayout noResultsMsg) {
        progressView.setVisibility(View.VISIBLE);

        String routeId = null;
        if (nextArrivalFavorite.getRouteDirectionModel() != null) {
            routeId = nextArrivalFavorite.getRouteDirectionModel().getRouteId();
        }

        Call<NextArrivalModelResponse> results = SeptaServiceFactory.getNextArrivalService().getNextArrival(Integer.parseInt(nextArrivalFavorite.getStart().getStopId()),
                Integer.parseInt(nextArrivalFavorite.getDestination().getStopId()),
                nextArrivalFavorite.getTransitType().name(), routeId);

        results.enqueue(new Callback<NextArrivalModelResponse>() {
            @Override
            public void onResponse(@NonNull Call<NextArrivalModelResponse> call, @NonNull Response<NextArrivalModelResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    NextArrivalModelResponseParser parser = new NextArrivalModelResponseParser(response.body());

                    // change to no results message if needed
                    if (parser.getResults().isEmpty()) {
                        updateViewWhenNoResultsFound(favoriteHeader, expandCollapseButton, noResultsMsg);
                    } else {
                        updateViewWhenResultsFound(favoriteHeader, expandCollapseButton, noResultsMsg);
                    }

                    tripView.setNextToArriveData(parser);
                }
                progressView.setVisibility(View.GONE);

                // this snackbar is being created to be auto-dismissed in order to remove persistent snackbar when connection is regained
                mListener.autoDismissSnackbar();
            }

            @Override
            public void onFailure(@NonNull Call<NextArrivalModelResponse> call, @NonNull Throwable t) {
                tripView.setNextToArriveData(new NextArrivalModelResponseParser());

                // show that NTA data unavailable for that favorites row
                updateViewWhenNoResultsFound(favoriteHeader, expandCollapseButton, noResultsMsg);

                mListener.showSnackbarNoConnection();

                progressView.setVisibility(View.GONE);
            }
        });
    }

    private void refreshTransitViewFavorite(TransitViewFavorite transitViewFavorite, TransitViewFavoriteViewHolder transitViewFavoriteViewHolder) {
        // show transitview alert icons if any of the 3 lines have that detour
        boolean advisory, alert, detour, weather;

        // get check for alerts in first route
        String routeId = transitViewFavorite.getFirstRoute().getRouteId();
        TransitType transitType = isTrolley(context, routeId) ? TransitType.TROLLEY : TransitType.BUS;
        Alert routeAlerts = SystemStatusState.getAlertForLine(transitType, routeId);
        advisory = routeAlerts.isAdvisory();
        alert = routeAlerts.isAlert() || routeAlerts.isSuspended();
        detour = routeAlerts.isDetour();
        weather = routeAlerts.isSnow();

        // check if the 2nd route has alerts
        if (transitViewFavorite.getSecondRoute() != null) {
            routeId = transitViewFavorite.getSecondRoute().getRouteId();
            transitType = isTrolley(context, routeId) ? TransitType.TROLLEY : TransitType.BUS;
            routeAlerts = SystemStatusState.getAlertForLine(transitType, routeId);
            advisory = advisory || routeAlerts.isAdvisory();
            alert = alert || routeAlerts.isAlert() || routeAlerts.isSuspended();
            detour = detour || routeAlerts.isDetour();
            weather = weather || routeAlerts.isSnow();

            // check if teh 3rd route has alerts
            if (transitViewFavorite.getThirdRoute() != null) {
                routeId = transitViewFavorite.getThirdRoute().getRouteId();
                transitType = isTrolley(context, routeId) ? TransitType.TROLLEY : TransitType.BUS;
                routeAlerts = SystemStatusState.getAlertForLine(transitType, routeId);
                advisory = advisory || routeAlerts.isAdvisory();
                alert = alert || routeAlerts.isAlert() || routeAlerts.isSuspended();
                detour = detour || routeAlerts.isDetour();
                weather = weather || routeAlerts.isSnow();
            }
        }

        if (advisory) {
            transitViewFavoriteViewHolder.advisoryIcon.setVisibility(View.VISIBLE);
        } else {
            transitViewFavoriteViewHolder.advisoryIcon.setVisibility(View.GONE);
        }

        if (alert) {
            transitViewFavoriteViewHolder.alertIcon.setVisibility(View.VISIBLE);
        } else {
            transitViewFavoriteViewHolder.alertIcon.setVisibility(View.GONE);
        }

        if (detour) {
            transitViewFavoriteViewHolder.detourIcon.setVisibility(View.VISIBLE);
        } else {
            transitViewFavoriteViewHolder.detourIcon.setVisibility(View.GONE);
        }

        if (weather) {
            transitViewFavoriteViewHolder.weatherIcon.setVisibility(View.VISIBLE);
        } else {
            transitViewFavoriteViewHolder.weatherIcon.setVisibility(View.GONE);
        }
    }

    public void updateList(List<FavoriteState> favoriteStateList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new FavoriteStateDiffCallback(this.mFavoriteStateList, favoriteStateList));
        this.mFavoriteStateList = favoriteStateList;

        // make changes to view
        diffResult.dispatchUpdatesTo(this);
    }

    public class NTAFavoriteViewHolder extends RecyclerView.ViewHolder {
        LinearLayout favoriteRow, favoriteHeader;
        TextView favoriteName;
        ImageButton expandCollapseButton;
        LinearLayout noResultsMsg;
        ViewGroup resultsContainer;
        View progressView;
        NextToArriveTripView tripView;

        NTAFavoriteViewHolder(final View view) {
            super(view);
            favoriteRow = view.findViewById(R.id.favorite_item_row);
            favoriteHeader = view.findViewById(R.id.favorite_item_header);
            favoriteName = view.findViewById(R.id.favorite_title_text);
            expandCollapseButton = view.findViewById(R.id.favorite_item_collapse_button);
            noResultsMsg = view.findViewById(R.id.favorite_item_no_results);
            resultsContainer = view.findViewById(R.id.next_to_arrive_trip_details);
            progressView = view.findViewById(R.id.progress_view);
            tripView = new NextToArriveTripView(context);
        }
    }

    public class TransitViewFavoriteViewHolder extends RecyclerView.ViewHolder {
        LinearLayout favoriteRow;
        TextView favoriteName, viewResults;
        ImageView advisoryIcon, alertIcon, detourIcon, weatherIcon;

        TransitViewFavoriteViewHolder(final View view) {
            super(view);
            favoriteRow = view.findViewById(R.id.favorite_item_row);
            favoriteName = view.findViewById(R.id.favorite_title_text);
            viewResults = view.findViewById(R.id.favorite_item_view_results);
            advisoryIcon = view.findViewById(R.id.advisory_icon);
            alertIcon = view.findViewById(R.id.alert_icon);
            detourIcon = view.findViewById(R.id.detour_icon);
            weatherIcon = view.findViewById(R.id.weather_icon);
        }
    }

    public interface FavoriteItemListener {
        void showSnackbarNoConnection();

        void autoDismissSnackbar();

        void goToSchedulesForTarget(NextArrivalFavorite nextArrivalFavorite);

        void goToNextToArrive(NextArrivalFavorite nextArrivalFavorite);

        void goToTransitView(TransitViewFavorite transitViewFavorite);
    }
}