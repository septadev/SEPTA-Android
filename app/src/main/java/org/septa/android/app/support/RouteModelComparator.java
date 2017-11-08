package org.septa.android.app.support;

import android.support.annotation.NonNull;

import org.septa.android.app.domain.RouteDirectionModel;

import java.util.Comparator;

/**
 * Created by jkampf on 8/15/17.
 */


public class RouteModelComparator implements Comparator<RouteDirectionModel> {

    @Override
    public int compare(RouteDirectionModel x, RouteDirectionModel y) {
        if (x == y) return 0;
        if (x == null)
            return Integer.MIN_VALUE;
        if (y == null)
            return Integer.MAX_VALUE;

        ShortRouteNameParts xPart = getParts(x);
        ShortRouteNameParts yPart = getParts(y);

        return xPart.compareTo(yPart);
    }

    private ShortRouteNameParts getParts(RouteDirectionModel routeDirectionModel){
        ShortRouteNameParts returnShortRouteNameParts = new ShortRouteNameParts();

        StringBuilder numberStringBuilder = new StringBuilder();
        int i = 0;
        while (i < routeDirectionModel.getRouteShortName().length() && Character.isDigit(routeDirectionModel.getRouteShortName().charAt(i))) {
            numberStringBuilder.append(routeDirectionModel.getRouteShortName().charAt(i++));
        }

        if (i > 0)
            returnShortRouteNameParts.number = Double.parseDouble(numberStringBuilder.toString());
        else returnShortRouteNameParts.number = Double.MAX_VALUE;

        returnShortRouteNameParts.text = routeDirectionModel.getRouteShortName().substring(i);

        return returnShortRouteNameParts;
    }

    private class ShortRouteNameParts implements Comparable<ShortRouteNameParts>{
        double number;
        String text;

        @Override
        public int compareTo(@NonNull ShortRouteNameParts shortRouteNameParts) {
            if (number != shortRouteNameParts.number)
                return (int) (number - shortRouteNameParts.number);

            return text.compareTo(shortRouteNameParts.text);
        }
    }
}
