package org.septa.android.app.favorites;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.nextarrive.NextArrivalModelResponseParser;
import org.septa.android.app.nextarrive.NextToArriveTripView;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Favorite;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalModelResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class FavoriteItemAdapter extends RecyclerView.Adapter<FavoriteItemAdapter.FavoriteViewHolder> {

    private static final String TAG = FavoriteItemAdapter.class.getSimpleName();

    private Context context;
    private int mLayoutId;
    private FavoriteItemListener mListener;
    private Map<String, FavoriteViewHolder> favoriteItemViews;
    private List<FavoriteState> mItemList;

    FavoriteItemAdapter(Context context, List<FavoriteState> list, int layoutId, FavoriteItemListener favoriteItemListener) {
        this.context = context;
        this.mItemList = list;
        mLayoutId = layoutId;
        mListener = favoriteItemListener;
        favoriteItemViews = new HashMap<>();
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FavoriteViewHolder holder, final int position) {
        final FavoriteState favoriteState = mItemList.get(position);
        String favoriteKey = favoriteState.getFavoriteKey();
        Favorite tempFavorite = SeptaServiceFactory.getFavoritesService().getFavoriteByKey(context, favoriteKey);

        if (tempFavorite == null) {
            SeptaServiceFactory.getFavoritesService().resyncFavoritesMap(context);
            tempFavorite = SeptaServiceFactory.getFavoritesService().getFavoriteByKey(context, favoriteKey);
        }

        final Favorite favorite = tempFavorite;

        // favorite name
        holder.favoriteName.setText(favorite.getName());

        // transit type icon on left
        Drawable drawables[] = holder.favoriteName.getCompoundDrawables();
        holder.favoriteName.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context,
                favorite.getTransitType().getTabActiveImageResource()),
                drawables[1], drawables[2], drawables[3]);

        // progress view
        holder.progressView.setVisibility(View.VISIBLE);

        // NTA results container
        holder.tripView.setMaxResults(3);
        holder.tripView.setTransitType(favorite.getTransitType());
        holder.tripView.setStart(favorite.getStart());
        holder.tripView.setDestination(favorite.getDestination());
        holder.tripView.setRouteDirectionModel(favorite.getRouteDirectionModel());
        holder.resultsContainer.removeAllViews();
        holder.resultsContainer.addView(holder.tripView);

        // refresh favorite results
        refreshFavorite(favorite, holder.favoriteHeader, holder.tripView, holder.progressView, holder.expandCollapseButton, holder.noResultsMsg);

        // initialize expanded state of favorite
        if (favoriteState.isExpanded()) {
            expandFavorite(holder.resultsContainer, holder.expandCollapseButton);
        } else {
            collapseFavorite(holder.resultsContainer, holder.expandCollapseButton);
        }

        // toggle expand / collapse button
        holder.favoriteHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // collapse favorite if already expanded
                // save expanded / collapsed state to service
                if (favoriteState.isExpanded()) {
                    collapseFavorite(holder.resultsContainer, holder.expandCollapseButton);
                    favoriteState.setExpanded(false);
                    SeptaServiceFactory.getFavoritesService().modifyFavoriteState(context, holder.getAdapterPosition(), false);
                } else {
                    expandFavorite(holder.resultsContainer, holder.expandCollapseButton);
                    favoriteState.setExpanded(true);
                    SeptaServiceFactory.getFavoritesService().modifyFavoriteState(context, holder.getAdapterPosition(), true);
                }
            }
        });

        // clicking on no results message navigates to prepopulated schedule selection picker
        holder.noResultsMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.goToSchedulesForTarget(favorite);
            }
        });

        // clicking on results opens NTA Results
        holder.resultsContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.goToNextToArrive(favorite);
            }
        });

        // add to map of views
        favoriteItemViews.put(favoriteKey, holder);
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
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
        this.mItemList.clear();
        this.mItemList.addAll(favoriteStateList);

        // update UI for favorites
        notifyDataSetChanged();
    }

    private void refreshFavorite(final Favorite favorite, final LinearLayout favoriteHeader, final NextToArriveTripView tripView, final View progressView, final ImageButton expandCollapseButton, final LinearLayout noResultsMsg) {
        progressView.setVisibility(View.VISIBLE);

        String routeId = null;
        if (favorite.getRouteDirectionModel() != null) {
            routeId = favorite.getRouteDirectionModel().getRouteId();
        }

        Call<NextArrivalModelResponse> results = SeptaServiceFactory.getNextArrivalService().getNextArrival(Integer.parseInt(favorite.getStart().getStopId()),
                Integer.parseInt(favorite.getDestination().getStopId()),
                favorite.getTransitType().name(), routeId);

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

    public void updateList(List<FavoriteState> favoriteStateList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new FavoriteStateDiffCallback(this.mItemList, favoriteStateList));
        this.mItemList = favoriteStateList;

        // make changes to view
        diffResult.dispatchUpdatesTo(this);
    }

    public class FavoriteViewHolder extends RecyclerView.ViewHolder {
        LinearLayout favoriteRow, favoriteHeader;
        TextView favoriteName;
        ImageButton expandCollapseButton;
        LinearLayout noResultsMsg;
        ViewGroup resultsContainer;
        View progressView;
        NextToArriveTripView tripView;

        FavoriteViewHolder(final View view) {
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

    public interface FavoriteItemListener {
        void showSnackbarNoConnection();
        void autoDismissSnackbar();
        void goToSchedulesForTarget(Favorite favorite);
        void goToNextToArrive(Favorite favorite);
    }
}