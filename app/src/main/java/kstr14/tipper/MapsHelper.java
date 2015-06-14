package kstr14.tipper;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseGeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Class containing helper methods for dealing with locations
 */
public class MapsHelper {

    /**
     * Convert Location object to LatLng object
     * @param location
     * @return
     */
    public static LatLng getLatLngFromLocation(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        LatLng latLng = new LatLng(lat, lon);
        return latLng;
    }

    /**
     * Convert LatLng object to Location object
     * @param point
     * @return
     */
    public static Location getLocationFromLatLng(LatLng point) {
        Location location =  new Location("Location");
        location.setLatitude(point.latitude);
        location.setLongitude(point.longitude);
        return location;
    }

    /**
     * Creating String representation of address from LatLng object
     * @param latLng
     * @param context
     * @return
     */
    public static String getAddressFromLatLng(LatLng latLng, Context context) {
        Geocoder geocoder;
        List<Address> addresses = new ArrayList<Address>();
        geocoder = new Geocoder(context, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(!addresses.isEmpty()) {
            String address = addresses.get(0).getAddressLine(0) + "\n"
                    + addresses.get(0).getPostalCode() + " "
                    + addresses.get(0).getLocality();
            return address;
        } else return null;
    }

    /**
     * Convert LatLng object to ParseGeoPoint object
     * @param latLng
     * @return
     */
    public static ParseGeoPoint getParseGeoPointFromLatLng(LatLng latLng) {
        return new ParseGeoPoint(latLng.latitude, latLng.longitude);
    }

    /**
     * Convert ParseGeoPoint object to LatLng object
     * @param parseGeoPoint
     * @return
     */
    public static LatLng getLatLngFromParseGeoPoint(ParseGeoPoint parseGeoPoint) {
        return new LatLng(parseGeoPoint.getLatitude(), parseGeoPoint.getLongitude());
    }
}
