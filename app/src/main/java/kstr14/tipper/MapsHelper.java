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
 * Created by Kristine on 31-05-2015.
 */
public class MapsHelper {

    public static LatLng getLatLngFromLocation(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        LatLng latLng = new LatLng(lat, lon);
        return latLng;
    }

    public static Location getLocationFromLatLng(LatLng point) {
        Location location =  new Location("Location");
        location.setLatitude(point.latitude);
        location.setLongitude(point.longitude);
        return location;
    }

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

    public static ParseGeoPoint getParseGeoPointFromLatLng(LatLng latLng) {
        return new ParseGeoPoint(latLng.latitude, latLng.longitude);
    }
}
