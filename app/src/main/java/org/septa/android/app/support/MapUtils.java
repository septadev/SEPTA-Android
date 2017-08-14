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

import org.septa.android.app.R;
import org.septa.android.app.domain.KMLModel;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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

    public static void drawTrainLine(GoogleMap googleMap, Context context, List<String> linesToDraw) {
        Resources res = context.getResources();
        InputStream inputStream = res.openRawResource(R.raw.regionalrail);
        KMLModel kmlModel = readKMLFile(inputStream);

        List<String> linesToDrawModified = translateToKmlNames(linesToDraw);

        // loop through the placemarks
        List<KMLModel.Document.Placemark> placemarkList = kmlModel.getDocument().getPlacemarkList();
        for (KMLModel.Document.Placemark placemark : placemarkList) {
            String lineName = placemark.getName();
            boolean found = false;
            for (String drawLine : linesToDrawModified) {
                if (lineName.equals(drawLine)) {
                    found = true;
                    break;
                }
            }

            if (found) {
                List<KMLModel.Document.MultiGeometry.LineString> lineStringList = placemark.getMultiGeometry().getLineStringList();
                for (KMLModel.Document.MultiGeometry.LineString lineString : lineStringList) {
                    String color = "#" + kmlModel.getDocument().getColorForStyleId(placemark.getStyleUrl());
                    List<LatLng> latLngCoordinateList = lineString.getLatLngCoordinates();

                    PolylineOptions lineOptions = new PolylineOptions().addAll(latLngCoordinateList)
                            .color(Color.parseColor(color))
                            .width(3.0f)
                            .visible(true);
                    googleMap.addPolyline(lineOptions);
                }
            }
        }

    }


    public static KMLModel readKMLFile(InputStream input) {
        Document xmlDocument = null;

        KMLModel kmlModel = new KMLModel();
        KMLModel.Document document = kmlModel.createDocument();
        try {
            // create a document builder to create the XML document object.
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            xmlDocument = builder.parse(new InputSource(input));

            // create the xPath object.
            XPath xPath = XPathFactory.newInstance().newXPath();

            document.createStyleList();

            //document/style
            XPathExpression tag_id = xPath.compile("/kml/Document/Style");
            NodeList styleNodeList = (NodeList) tag_id.evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < styleNodeList.getLength(); i++) {
                KMLModel.Document.Style style = new KMLModel.Document.Style();
                Node n = styleNodeList.item(i);

                String id = n.getAttributes().getNamedItem("id").getNodeValue();
                style.setId(id);

                tag_id = xPath.compile("LineStyle/color");
                String lineStyleColor = tag_id.evaluate(n);
                String repositionedColorString;
                if (lineStyleColor.length() >= 8) {
                    repositionedColorString = lineStyleColor.substring(0, 2) +
                            lineStyleColor.substring(6, 8) +
                            lineStyleColor.substring(4, 6) +
                            lineStyleColor.substring(2, 4);
                } else {
                    repositionedColorString = "ff000000";
                }
                style.createLineStyle().setColor(repositionedColorString);

                document.getStyleList().add(style);
            }

            tag_id = xPath.compile("//Placemark");
            NodeList placemarkNodeList = (NodeList) tag_id.evaluate(xmlDocument, XPathConstants.NODESET);

            ArrayList<KMLModel.Document.Placemark> placemarks = (ArrayList<KMLModel.Document.Placemark>) document.createPlacemarkList();
            for (int i = 0; i < placemarkNodeList.getLength(); i++) {
                KMLModel.Document.Placemark placemark = new KMLModel.Document.Placemark();

                Node n = placemarkNodeList.item(i);

                tag_id = xPath.compile("name");
                String name = tag_id.evaluate(n);
                placemark.setName(name);

                tag_id = xPath.compile("styleUrl");
                String styleUrl = tag_id.evaluate(n).substring(1);
                placemark.setStyleUrl(styleUrl);

                tag_id = xPath.compile("MultiGeometry/LineString/coordinates");
                NodeList coordinatesNodeList = (NodeList) tag_id.evaluate(n, XPathConstants.NODESET);

                ArrayList<KMLModel.Document.MultiGeometry.LineString> lineStrings = (ArrayList<KMLModel.Document.MultiGeometry.LineString>) placemark.createMultiGeometry().createLineStringList();
                for (int j = 0; j < coordinatesNodeList.getLength(); j++) {
                    KMLModel.Document.MultiGeometry.LineString lineString = new KMLModel.Document.MultiGeometry.LineString();
                    String coordinatesString = coordinatesNodeList.item(j).getFirstChild().getNodeValue();
                    lineString.setRawCoordinateString(coordinatesString.trim());

                    lineString.processRawCoordinatesString();

                    lineStrings.add(lineString);
                }

                placemarks.add(placemark);
            }
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
            ;
        }
        return kmlModel;
    }

    private static List<String> translateToKmlNames(List<String> linesToDraw) {
        List<String> returnList = new ArrayList<String>(linesToDraw.size());
        for (int i = 0; i < linesToDraw.size(); i++) {
            if (linesToDraw.get(i).toLowerCase().contains("paoli"))
                returnList.add("R5 Thorndale");

            if (linesToDraw.get(i).toLowerCase().startsWith("airport"))
                returnList.add("R1 Airport");
        }

        return returnList;
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
        if (permissionCheck == PackageManager.PERMISSION_GRANTED)
        {
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
