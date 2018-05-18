package org.septa.android.app.favorites;

import org.septa.android.app.TransitType;
import org.septa.android.app.domain.RouteDirectionModel;
import org.septa.android.app.domain.StopModel;

/**
 * Created by jkampf on 10/5/17.
 */

public interface FavoritesFragmentListener {

    public void refresh();

    public void addNewFavorite();

    public void gotoSchedules();

    public void goToSchedulesForTarget(StopModel start, StopModel destination, TransitType transitType, RouteDirectionModel routeDirectionModel);

}
