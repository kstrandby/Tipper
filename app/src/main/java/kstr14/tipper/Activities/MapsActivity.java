package kstr14.tipper.Activities;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import kstr14.tipper.MapsHelper;
import kstr14.tipper.R;

/**
 * Activity showing maps fragment allowing a user to specify a location either by navigating
 * in the map or by searching for a location by name
 * The activity contains two other fragments used for searching and specifying current chosen
 * location:
 * - ShowLocationFragment: Showing the name of the currently chosen location in a box on top of map
 * - ShowSearchLocationFragment: Showing an EditText field allowing user to search for a location
 * on top of the map
 */
public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        OnInfoWindowClickListener {

    private final static int MAX_SEARCH_RESULTS = 5;

    private GoogleMap map;
    private Geocoder geoCoder;
    private Marker marker;
    private LocationManager locationManager;
    private Location location;
    private ShowLocationFragment showLocationFragment;
    private ShowSearchLocationFragment showSearchLocationFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        geoCoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        // setup map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        map = mapFragment.getMap();
        map.setMyLocationEnabled(true);
        map.setOnMarkerClickListener(this);
        map.setOnInfoWindowClickListener(this);

        // set custom fragment to show the current chosen location
        showLocationFragment = new ShowLocationFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.location_fragment, showLocationFragment).commit();
        getFragmentManager().executePendingTransactions();

        // set the default map to show last known location of user
        locationManager = (LocationManager)this.getSystemService(LOCATION_SERVICE);
        location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        LatLng latLng = MapsHelper.getLatLngFromLocation(location);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));

        // move camera to my current location and update TextView to address of my current location
        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                location = map.getMyLocation();
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(MapsHelper.getLatLngFromLocation(location), 16.0f));
                updateAddress();
                return true;
            }
        });

        // create marker and update TextView with address when point in map is clicked
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                map.clear();
                marker = map.addMarker(new MarkerOptions().position(point));
                location = MapsHelper.getLocationFromLatLng(point);
                updateAddress();
            }
        });
    }

    /**
     * Fetches the specified location and returns to the calling activity
     * @param view
     */
    public void submitLocation(View view) {
        LatLng latLng = MapsHelper.getLatLngFromLocation(location);
        Intent intent = new Intent();
        intent.putExtra("latLng", latLng);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Creates the ActionBar menu
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_maps, menu);
        return true;
    }

    /**
     * Handles ActionBar items clicks
     * As the ActionBar is hidden on the maps screen, this is not used
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when search button on ShowLocationFragment is clicked
     * Switches the ShowLocationFragment with the ShowSearchLocationFragment
     * @param view
     */
    public void searchClicked(View view) {
        showSearchLocationFragment = new ShowSearchLocationFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Replace the ShowLocationFragment with the ShowSearchLocationFragment,
        // and add the transaction to the back stack so the user can navigate back
        fragmentTransaction.replace(R.id.location_fragment, showSearchLocationFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    /**
     * Called when the search button on ShowSearchLocationFragment is clicked
     * Fetches the query and performs a location search using Google Maps
     * Places a marker on the first 5 search results and zoomes the view to show all markers
     *
     * @param view
     * @throws IOException
     */
    public void searchAddress(View view) throws IOException {
        map.clear();
        String query = showSearchLocationFragment.getAddressInput().getText().toString();

        // search for results
        List<Address> results = geoCoder.getFromLocationName(query, MAX_SEARCH_RESULTS);

        // add a marker for each result and create a new bounds to be able to show all markers
        if(results != null && !results.isEmpty()){
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for(Address address : results) {
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                Marker marker = map.addMarker(new MarkerOptions().position(latLng)
                        .title(address.getAddressLine(0))
                        .snippet(address.getLocality() + " " + address.getPostalCode()));
                marker.showInfoWindow();
                builder.include(latLng);
            }

            LatLngBounds bounds = builder.build();
            int padding = 20; // offset from edges of the map in pixels
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            map.animateCamera(cameraUpdate);
        } else {
            Toast.makeText(getApplicationContext(), "No results for search.", Toast.LENGTH_SHORT).show();
        }

        // hide keyboard
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * Handles clicks on markers
     * @param marker
     * @return
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        markerClicked(marker);
        return true;
    }

    /**
     * Handles clicks on InfoWindows on markers
     * Behaves identically to onMarkerClicks
     * @param marker
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        markerClicked(marker);
    }

    /**
     * Update the address shown in the TextView in ShowLocationFragment
     */
    public void updateAddress() {
        String address = MapsHelper
                .getAddressFromLatLng(MapsHelper.getLatLngFromLocation(location), getApplicationContext());
        if(address != null) {
            showLocationFragment.getLocationView().setText(address);
        }
    }

    /**
     * Switches the ShowSearchLocationFragment for the ShowLocationFragment and updates the TextView
     * on the ShowLocationFragment with the address of the marker clicked
     * @param marker
     */
    public void markerClicked(Marker marker) {
        marker.showInfoWindow();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.location_fragment, showLocationFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        location = MapsHelper.getLocationFromLatLng(marker.getPosition());
        updateAddress();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) { }
}
