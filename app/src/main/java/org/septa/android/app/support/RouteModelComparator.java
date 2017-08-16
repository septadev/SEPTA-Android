package org.septa.android.app.support;

import android.support.annotation.NonNull;

import org.septa.android.app.domain.RouteModel;

import java.util.Comparator;

/**
 * Created by jkampf on 8/15/17.
 */


public class RouteModelComparator implements Comparator<RouteModel> {

    @Override
    public int compare(RouteModel x, RouteModel y) {
        ShortRouteNameParts xPart = getParts(x);
        ShortRouteNameParts yPart = getParts(y);

        return xPart.compareTo(yPart);
    }

    private ShortRouteNameParts getParts(RouteModel routeModel){
        ShortRouteNameParts returnShortRouteNameParts = new ShortRouteNameParts();

        StringBuilder numberStringBuilder = new StringBuilder();
        int i = 0;
        while (i < routeModel.getRouteShortName().length() && Character.isDigit(routeModel.getRouteShortName().charAt(i))) {
            numberStringBuilder.append(routeModel.getRouteShortName().charAt(i++));
        }

        if (i > 0)
            returnShortRouteNameParts.number = Double.parseDouble(numberStringBuilder.toString());
        else returnShortRouteNameParts.number = Double.MAX_VALUE;

        returnShortRouteNameParts.text = routeModel.getRouteShortName().substring(i);

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
