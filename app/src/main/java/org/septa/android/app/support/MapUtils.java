package org.septa.android.app.support;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.data.kml.KmlLayer;

import org.septa.android.app.R;
import org.septa.android.app.domain.KMLModel;
import org.septa.android.app.nextarrive.NextToArriveResultsActivity;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

/**
 * Created by jkampf on 8/10/17.
 */

public class MapUtils {

    public static KmlLayer getKMLByLineId(Context context, GoogleMap googleMap, String lineId) {
        int resourceId = context.getResources().getIdentifier("kml_" + lineId.toLowerCase(), "raw", R.class.getPackage().getName());
        try {
            return new KmlLayer(googleMap, resourceId, context);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Location getLocationFromAddress(Context context, String strAddress) {
        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();
            Location returnLocation = new Location(strAddress);
            returnLocation.setLatitude(location.getLatitude());
            returnLocation.setLongitude(location.getLongitude());


            return returnLocation;

        } catch (IOException ex) {

            ex.printStackTrace();
            return null;
        }

    }

    public static String getCurrentAddress(Context context) {
        Geocoder coder = new Geocoder(context);

        int permissionCheck = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Task<Location> locationTask = LocationServices.getFusedLocationProviderClient(context).getLastLocation();
            while (!locationTask.isComplete()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Location loc = locationTask.getResult();
            if (loc != null) {
                try {
                    List<Address> results = coder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                    if (results != null && results.size() > 0) {
                        StringBuilder builder = new StringBuilder(results.get(0).getAddressLine(0)).append(" ").append(results.get(0).getLocality()).append(" ").append(results.get(0).getAdminArea()).append(" ").append(results.get(0).getPostalCode());
                        return builder.toString();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }
        return null;
    }
}
