package kstr14.tipper.Activities;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.plus.Plus;
import com.parse.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kstr14.tipper.Application;
import kstr14.tipper.Data.TipperUser;
import kstr14.tipper.MapsHelper;
import kstr14.tipper.R;

/**
 * Activity allowing a user to search for a tip by keyword, location, price and category
 */
public class SearchTipActivity extends ActionBarActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // ACTIVITY_ID is used for logging and keeping track of navigation between activities
    public static final String ACTIVITY_ID = "SearchTipActivity";

    // UI elements
    private EditText keywordInput;
    private EditText locationInput;
    private SeekBar priceSeekBarInput;
    private TextView priceView;
    private CheckBox foodCheckBox;
    private CheckBox drinksCheckBox;
    private CheckBox otherCheckBox;
    private int chosenPrice;

    private GoogleApiClient googleApiClient;
    public static String defaultLocation = "Current location";
    private LatLng latLngPosition;
    private Geocoder geoCoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_tip);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        geoCoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .build();
        googleApiClient.connect();

        // initialize UI elements
        keywordInput = (EditText) findViewById(R.id.searchTip_ed_keywords);
        locationInput = (EditText) findViewById(R.id.searchTip_ed_location);
        priceSeekBarInput = (SeekBar) findViewById(R.id.searchTip_sb_price);
        priceView = (TextView) findViewById(R.id.searchTip_tv_price);
        foodCheckBox = (CheckBox) findViewById(R.id.searchTip_cb_food);
        drinksCheckBox = (CheckBox) findViewById(R.id.searchTip_cb_drinks);
        otherCheckBox = (CheckBox) findViewById(R.id.searchTip_cb_other);

        // set default text on location input field unless the field is being edited
        locationInput.setText(defaultLocation);
        locationInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && TextUtils.isEmpty(locationInput.getText().toString())) {
                    locationInput.setText(defaultLocation);
                } else if (hasFocus && locationInput.getText().toString().equals(defaultLocation)) {
                    locationInput.setText("");
                }
            }
        });

        // handle price SeekBar changes
        priceSeekBarInput.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                priceView.setText("Price: " + progress + " AUD");
                chosenPrice = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // record user's position for location search
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                latLngPosition = MapsHelper.getLatLngFromLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    /**
     * Creates the ActionBar menu
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_tip, menu);
        return true;
    }

    /**
     * Called when the SEARCH button is clicked
     * The intent is filled with search query requirements specified
     * by the user on the UI (i.e. prices, keyword, etc.)
     *
     * @param view
     */
    public void searchTip(View view) throws IOException {
        // Preparing extras for the Intent
        String keyword = keywordInput.getText().toString();
        String location = locationInput.getText().toString();

        if(!location.equals(defaultLocation)) {
            // search for location results and save the first result
            List<Address> results = geoCoder.getFromLocationName(location, 1);
            if(results != null && !results.isEmpty()) {
                latLngPosition = new LatLng(results.get(0).getLatitude(), results.get(0).getLongitude());
            }
        }

        // set up the intent
        List<String> categories = new ArrayList<>();
        if (foodCheckBox.isChecked()) categories.add("Food");
        if (drinksCheckBox.isChecked()) categories.add("Drinks");
        if (otherCheckBox.isChecked()) categories.add("Other");
        String[] categoriesArray = categories.toArray(new String[categories.size()]);

        Intent intent = new Intent(this, ListActivity.class);
        intent.putExtra("source", ACTIVITY_ID);
        intent.putExtra("keyword", keyword);
        intent.putExtra("location", latLngPosition);
        intent.putExtra("categories", categoriesArray);
        intent.putExtra("maxPrice", chosenPrice);

        startActivity(intent);
    }

    /**
     * Handles ActionBar click events
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handling action bar events
        int id = item.getItemId();
        if (id == R.id.groups) {
            Intent intent = new Intent(this, MyGroupsActivity.class);
            intent.putExtra("source", ACTIVITY_ID);
            startActivity(intent);
            return true;
        } else if (id == R.id.favourites) {
            Intent intent = new Intent(this, ListActivity.class);
            intent.putExtra("source", ACTIVITY_ID);
            intent.putExtra("context", "favourites");
            startActivity(intent);
            return true;
        } else if (id == R.id.profile) {
            Intent intent = new Intent(this, MyProfileActivity.class);
            intent.putExtra("source", ACTIVITY_ID);
            startActivity(intent);
            return true;
        } else if (id == R.id.main_menu_logout) {
            TipperUser user = ((Application) getApplicationContext()).getCurrentUser();
            if (user.isGoogleUser()) {
                Log.d(ACTIVITY_ID, "Google user signing out.....");
                if (googleApiClient.isConnected()) {
                    Plus.AccountApi.clearDefaultAccount(googleApiClient);
                    googleApiClient.disconnect();
                    Log.d(ACTIVITY_ID, "googleApiClient was connected, user is signed out now");
                } else {
                    Log.e(ACTIVITY_ID, "Trying to log out user, but GoogleApiClient was disconnected");
                }
            } else if(user.isFacebookUser()) {
                Log.d(ACTIVITY_ID, "Facebook user signing out......");
                FacebookSdk.sdkInitialize(getApplicationContext());
                LoginManager.getInstance().logOut();
            }
            try {
                ((Application) getApplicationContext()).getCurrentUser().unpin();
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
            }
            ((Application) getApplicationContext()).setCurrentUser(null);
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.about) {
            Intent intent = new Intent(this, AboutActivity.class);
            intent.putExtra("source", ACTIVITY_ID);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Used for navigation with back button on ActionBar
     *
     * @return
     */
    @Override
    public Intent getSupportParentActivityIntent() {
        return getParentActivity();
    }

    /**
     * Used for navigation with back button on ActionBar
     *
     * @return
     */
    @Override
    public Intent getParentActivityIntent() {
        return getParentActivity();
    }

    /**
     * Used for navigation with back button on actionbar
     * Calls onBackPressed to implement same behaviour as hardware back button clicks
     *
     * @return
     */
    private Intent getParentActivity() {
        onBackPressed();
        return null;
    }

    /**
     * methods below required only for use of GoogleApiClient, which is necessary for logout **
     */
    @Override
    public void onConnected(Bundle bundle) { }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { }
}
