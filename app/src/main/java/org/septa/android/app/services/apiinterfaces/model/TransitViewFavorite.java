package org.septa.android.app.services.apiinterfaces.model;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import org.septa.android.app.BuildConfig;
import org.septa.android.app.domain.RouteDirectionModel;

import static org.septa.android.app.transitview.TransitViewUtils.isTrolley;

public class TransitViewFavorite extends Favorite {

    // the "TRANSITVIEW_" prefix is used to determine which layout to use in favorites screen
    public static final String TRANSITVIEW = "TRANSITVIEW";

    @NonNull
    @SerializedName("first_route")
    private RouteDirectionModel firstRoute;

    @SerializedName("second_route")
    private RouteDirectionModel secondRoute;

    @SerializedName("third_route")
    private RouteDirectionModel thirdRoute;

    public TransitViewFavorite(Context context, @NonNull RouteDirectionModel first, RouteDirectionModel second, RouteDirectionModel third) {
        this.firstRoute = first;
        this.secondRoute = second;
        this.thirdRoute = third;

        StringBuilder favoriteName = new StringBuilder(firstRoute.getRouteShortName());
        if (isTrolley(context, firstRoute.getRouteId())) {
            favoriteName.append(" Trolley");
        } else {
            favoriteName.append(" Bus");
        }

        if (secondRoute != null) {
            favoriteName.append(", ")
                    .append(secondRoute.getRouteShortName());
            if (isTrolley(context, secondRoute.getRouteId())) {
                favoriteName.append(" Trolley");
            } else {
                favoriteName.append(" Bus");
            }

            if (thirdRoute != null) {
                favoriteName.append(", ")
                        .append(thirdRoute.getRouteShortName());
                if (isTrolley(context, thirdRoute.getRouteId())) {
                    favoriteName.append(" Trolley");
                } else {
                    favoriteName.append(" Bus");
                }
            }
        }

        name = favoriteName.toString();

        createdWithVersion = BuildConfig.VERSION_CODE;
    }

    @Override
    public String getKey() {
        String secondRouteId = null, thirdRouteId = null;
        if (secondRoute != null) {
            secondRouteId = secondRoute.getRouteId();

            if (thirdRoute != null) {
                thirdRouteId = thirdRoute.getRouteId();
            }
        }
        return generateKey(firstRoute.getRouteId(), secondRouteId, thirdRouteId);
    }

    private static String generateKey(String firstRouteId, String secondRouteId, String thirdRouteId) {
        StringBuilder favoriteKey = new StringBuilder(TRANSITVIEW);
        favoriteKey.append(FAVORITE_KEY_DELIM).append(firstRouteId);

        if (secondRouteId != null) {
            favoriteKey.append(FAVORITE_KEY_DELIM).append(secondRouteId);

            if (thirdRouteId != null) {
                favoriteKey.append(FAVORITE_KEY_DELIM).append(thirdRouteId);
            }
        }
        return favoriteKey.toString();
    }

    public static String generateKey(@NonNull RouteDirectionModel firstRoute, RouteDirectionModel secondRoute, RouteDirectionModel thirdRoute) {
        String secondRouteId = null, thirdRouteId = null;
        if (secondRoute != null) {
            secondRouteId = secondRoute.getRouteId();

            if (thirdRoute != null) {
                thirdRouteId = thirdRoute.getRouteId();
            }
        }
        return generateKey(firstRoute.getRouteId(), secondRouteId, thirdRouteId);
    }

    @NonNull
    public RouteDirectionModel getFirstRoute() {
        return firstRoute;
    }

    public void setFirstRoute(@NonNull RouteDirectionModel firstRoute) {
        this.firstRoute = firstRoute;
    }

    public RouteDirectionModel getSecondRoute() {
        return secondRoute;
    }

    public void setSecondRoute(RouteDirectionModel secondRoute) {
        this.secondRoute = secondRoute;
    }

    public RouteDirectionModel getThirdRoute() {
        return thirdRoute;
    }

    public void setThirdRoute(RouteDirectionModel thirdRoute) {
        this.thirdRoute = thirdRoute;
    }

    @Override
    public String toString() {
        return "TransitViewFavorite{" +
                "firstRoute=" + firstRoute +
                ", secondRoute=" + secondRoute +
                ", thirdRoute=" + thirdRoute +
                '}';
    }
}

