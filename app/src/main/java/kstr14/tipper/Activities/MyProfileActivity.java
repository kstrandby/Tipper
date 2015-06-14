package kstr14.tipper.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.parse.ParseException;

import kstr14.tipper.Application;
import kstr14.tipper.Data.TipperUser;
import kstr14.tipper.R;

/**
 * Activity showing details about the current user
 */
public class MyProfileActivity extends ActionBarActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // ACTIVITY_ID is used for logging and keeping track of navigation between activities
    public static final String ACTIVITY_ID = "MyProfileActivity";

    // UI elements
    private TextView usernameView;
    private TextView emailView;

    private TipperUser user;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        googleApiClient =  new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .build();
        googleApiClient.connect();

        user = ((Application)getApplicationContext()).getCurrentUser();

        // initialize UI elements
        usernameView = (TextView) findViewById(R.id.myProfile_tv_username);
        emailView = (TextView) findViewById(R.id.myProfile_tv_email);

        usernameView.setText(user.getUsername() + "\n");
        emailView.setText(user.getEmail() + "\n");
    }

    /**
     * Handles back button clicks on ActionBar
     *
     * @return
     */
    @Override
    public Intent getSupportParentActivityIntent() {
        return getParentActivity();
    }

    /**
     * Handles back button clicks on ActionBar
     *
     * @return
     */
    @Override
    public Intent getParentActivityIntent() {
        return getParentActivity();
    }

    /**
     * Handles back button clicks on ActionBar
     * Behaves identically to hardware back button clicks
     * @return
     */
    private Intent getParentActivity() {
        onBackPressed();
        return null;
    }

    /**
     * Creates the ActionBar menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my_profile, menu);
        return true;
    }

    /**
     * Handles ActionBar click events
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
        }  else if (id == R.id.main_menu_logout) {
            if(user.isGoogleUser()) {
                Log.d(ACTIVITY_ID, "Google user signing out.....");
                if(googleApiClient.isConnected()) {
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
                ((Application)getApplicationContext()).getCurrentUser().unpin();
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
            }
            ((Application)getApplicationContext()).setCurrentUser(null);
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

    /*** methods below required only for use of GoogleApiClient, which is necessary for logout ***/
    @Override
    public void onConnected(Bundle bundle) { }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { }
}
