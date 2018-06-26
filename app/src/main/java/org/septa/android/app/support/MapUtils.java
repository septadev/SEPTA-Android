package org.septa.android.app.support;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.data.kml.KmlLayer;

import org.septa.android.app.R;
import org.septa.android.app.TransitType;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public abstract class MapUtils {

    private static final String TAG = MapUtils.class.getSimpleName();

    public static KmlLayer getKMLByLineId(Context context, GoogleMap googleMap, String lineId, TransitType transitType) {
        int resourceId = context.getResources().getIdentifier("kml_" + lineId.toLowerCase(), "raw", R.class.getPackage().getName());

        // For some reason google maps is showing the colors of MFL and BSL reversed.
        // So the following code reverses that
        String modifiedLineId;
        if (lineId.equalsIgnoreCase("mfl")) {
            modifiedLineId = "bsl";
        } else if (lineId.equalsIgnoreCase("bsl")){
            modifiedLineId = "mfl";
        } else {
            modifiedLineId = lineId;
        }

        String colorValue = Integer.toHexString(ContextCompat.getColor(context, transitType.getLineColor(modifiedLineId, context)));
        InputStream raw = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            raw = context.getResources().openRawResource(resourceId);
            Document doc = db.parse(raw);
            XPath xPath = XPathFactory.newInstance().newXPath();

            // set line style color
            NodeList nodesColor = (NodeList) xPath.evaluate("//LineStyle/color",
                    doc.getDocumentElement(), XPathConstants.NODESET);
            for (int i = 0; i < nodesColor.getLength(); i++) {
                nodesColor.item(i).setTextContent(colorValue);
            }

            NodeList points = (NodeList) xPath.evaluate("//Placemark/MultiGeometry/Point",
                    doc.getDocumentElement(), XPathConstants.NODESET);
            for (int i = 0; i < points.getLength(); i++) {
                points.item(i).getParentNode().removeChild(points.item(i));
            }

            // remove line style width
            NodeList nodesWidth = (NodeList) xPath.evaluate("//LineStyle/width",
                    doc.getDocumentElement(), XPathConstants.NODESET);
            for (int i = 0; i < nodesWidth.getLength(); i++) {
                nodesWidth.item(i).getParentNode().removeChild(nodesWidth.item(i));
            }

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String xml = writer.toString();

            return new KmlLayer(googleMap, new ByteArrayInputStream(writer.toString().getBytes()), context);
        } catch (XmlPullParserException e) {
            Log.e(TAG, e.toString());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } catch (ParserConfigurationException e) {
            Log.e(TAG, e.toString());
        } catch (SAXException e) {
            Log.e(TAG, e.toString());
        } catch (XPathExpressionException e) {
            Log.e(TAG, e.toString());
        } catch (TransformerConfigurationException e) {
            Log.e(TAG, e.toString());
        } catch (TransformerException e) {
            Log.e(TAG, e.toString());
        } finally {
            if (raw != null) try {
                raw.close();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
        return null;
    }

    public static Location getLocationFromAddress(Context context, String strAddress) {
        Geocoder coder = new Geocoder(context);
        List<Address> address;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null || address.isEmpty()) {
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
                    Log.e(TAG, e.toString());
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
                    Log.e(TAG, e.toString());
                }
            }


        }
        return null;
    }
}
