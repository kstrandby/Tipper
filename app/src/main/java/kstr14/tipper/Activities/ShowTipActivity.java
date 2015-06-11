package kstr14.tipper.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.plus.Plus;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseImageView;
import com.parse.ParseQuery;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import kstr14.tipper.Application;
import kstr14.tipper.Data.Tip;
import kstr14.tipper.Data.TipperUser;
import kstr14.tipper.ErrorHandler;
import kstr14.tipper.ImageHelper;
import kstr14.tipper.MapsHelper;
import kstr14.tipper.R;

public class ShowTipActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String ACTIVITY_ID = "ShowTipActivity";

    private Tip tip;
    private ParseImageView imageView;
    private ImageButton upvoteButton;
    private ImageButton downvoteButton;
    private ImageButton favouritesButton;
    private TextView upvoteView;
    private TextView downvoteView;
    private TextView descriptionView;
    private TextView dateView;
    private TextView locationView;
    private TextView priceView;
    private ProgressDialog progressDialog;
    private String sourceActivity;

    private Bitmap upvoteBitmap;
    private Bitmap downvoteBitmap;
    private Bitmap favouritesBitmap;
    private Bitmap imageBitmap;

    private GoogleApiClient googleApiClient;

    private ShareActionProvider shareActionProvider;
    CallbackManager callbackManager;
    private LoginManager manager;
    ShareDialog shareDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tip);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .build();
        googleApiClient.connect();

        // initialize all UI elements
        imageView = (ParseImageView) findViewById(R.id.showTip_iv_tipImage);
        upvoteButton = (ImageButton) findViewById(R.id.showTip_ib_upvote);
        downvoteButton = (ImageButton) findViewById(R.id.showTip_ib_downvote);
        favouritesButton = (ImageButton) findViewById(R.id.showTip_ib_favourites);
        upvoteView = (TextView) findViewById(R.id.showTip_tv_upvotes);
        downvoteView = (TextView) findViewById(R.id.showTip_tv_downvotes);
        descriptionView = (TextView) findViewById(R.id.showTip_tv_description);
        dateView = (TextView) findViewById(R.id.showTip_tv_date);
        locationView = (TextView) findViewById(R.id.showTip_tv_location);
        priceView = (TextView) findViewById(R.id.showTip_tv_price);

        // set images of ImageButtons and ImageViews
        upvoteBitmap = ImageHelper.decodeBitmapFromResource(getResources(), R.drawable.thumbs_up, 128, 128);
        upvoteButton.setImageBitmap(Bitmap.createScaledBitmap(upvoteBitmap, 64, 64, false));
        downvoteBitmap = ImageHelper.decodeBitmapFromResource(getResources(), R.drawable.thumbs_down, 128, 128);
        downvoteButton.setImageBitmap(Bitmap.createScaledBitmap(downvoteBitmap, 64, 64, false));
        favouritesBitmap = ImageHelper.decodeBitmapFromResource(getResources(), R.drawable.star, 128, 128);
        favouritesButton.setImageBitmap(Bitmap.createScaledBitmap(favouritesBitmap, 64, 64, false));

        sourceActivity = getIntent().getExtras().getString("source");

        // show progressdialog while downloading Tip data
        progressDialog = ProgressDialog.show(this, "Loading", "Please wait while Tip is being loaded...");

        String ID = getIntent().getExtras().getString("ID");
        ParseQuery<Tip> query = ParseQuery.getQuery("Tip");
        query.whereEqualTo("uuid", ID);

        query.getFirstInBackground(new GetCallback<Tip>() {
            @Override
            public void done(Tip object, ParseException e) {
                progressDialog.dismiss();
                if (e == null && object != null) {
                    // set up the UI elements with the attributes of the Tip
                    tip = object;
                    String title = tip.getTitle();
                    getSupportActionBar().setTitle(title);

                    upvoteView.setText(String.valueOf(tip.getUpvotes()));
                    downvoteView.setText(String.valueOf(tip.getDownvotes()));
                    descriptionView.setText(tip.getDescription());

                    String dateText = prettyOutputDates();
                    dateView.setText(dateText);
                    priceView.setText("$ " + tip.getPrice());

                    ParseGeoPoint geoPoint = tip.getLocation();
                    if (geoPoint == null) {
                        locationView.setText("Location unknown");
                    } else {
                        LatLng latLng = MapsHelper.getLatLngFromParseGeoPoint(geoPoint);
                        String address = MapsHelper.getAddressFromLatLng(latLng, getApplicationContext());
                        locationView.setText(address);
                    }

                    // set default image if no image exist in database
                    ParseFile image = tip.getImage();
                    imageBitmap = ImageHelper.decodeBitmapFromResource(getResources(), R.drawable.food, 256, 256);
                    if (image == null) {
                        imageView.setImageBitmap(imageBitmap);
                    } else {
                        imageView.setPlaceholder(getResources().getDrawable(R.drawable.food));
                        imageView.setParseFile(image);
                        imageView.loadInBackground();
                    }
                } else {
                    // this case means connection error - show an alertdialog and go back to previous activity when OK clicked
                    ErrorHandler.showConnectionErrorAlert(ShowTipActivity.this, getParentActivity());
                    Log.e(ACTIVITY_ID, "Parse error: " + e.getStackTrace().toString());


                }
            }
        });

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();


    }

    @Override
    public void onStop() {
        super.onStop();
        if(upvoteBitmap != null) {
            upvoteBitmap.recycle();
            upvoteBitmap = null;
        }
        if(downvoteBitmap != null) {
            downvoteBitmap.recycle();
            downvoteBitmap = null;
        }
        if(favouritesBitmap != null) {
            favouritesBitmap.recycle();
            favouritesBitmap = null;
        }
        if(imageBitmap != null) {
            imageBitmap.recycle();
            imageBitmap = null;
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * Creates a nice String representation of the time period of the Tip
     * Takes into account if the Tip starts and ends at the same day, so only one date is shown and
     * the time is shown in the format hh:mm - hh:mm
     *
     * @return the produced String
     */
    private String prettyOutputDates() {
        Calendar start = Calendar.getInstance();
        start.setTime(tip.getStartDate());
        Calendar end = Calendar.getInstance();
        end.setTime(tip.getEndDate());
        String output = "";
        if (start.get(Calendar.YEAR) == end.get(Calendar.YEAR)
                && start.get(Calendar.MONTH) == end.get(Calendar.MONTH)
                && start.get(Calendar.DAY_OF_MONTH) == end.get(Calendar.DAY_OF_MONTH)) {

            // start and end is the same day
            output = start.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US) + " "
                    + String.format("%02d", start.get(Calendar.DAY_OF_MONTH)) + "-"
                    + String.format("%02d", start.get(Calendar.MONTH) + 1) + "-"
                    + start.get(Calendar.YEAR) + "\n"
                    + String.format("%02d", start.get(Calendar.HOUR_OF_DAY)) + ":"
                    + String.format("%02d", start.get(Calendar.MINUTE)) + "-"
                    + String.format("%02d", end.get(Calendar.HOUR_OF_DAY)) + ":"
                    + String.format("%02d", end.get(Calendar.MINUTE));
        } else {
            output = start.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US) + " "
                    + String.format("%02d", start.get(Calendar.DAY_OF_MONTH)) + "-"
                    + String.format("%02d", start.get(Calendar.MONTH) + 1) + "-"
                    + start.get(Calendar.YEAR) + " "
                    + String.format("%02d", start.get(Calendar.HOUR_OF_DAY)) + ":"
                    + String.format("%02d", start.get(Calendar.MINUTE)) + "\n-"
                    + end.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US) + " "
                    + String.format("%02d", end.get(Calendar.DAY_OF_MONTH)) + "-"
                    + String.format("%02d", end.get(Calendar.MONTH) + 1) + "-"
                    + end.get(Calendar.YEAR) + " "
                    + String.format("%02d", end.get(Calendar.HOUR_OF_DAY)) + ":"
                    + String.format("%02d", end.get(Calendar.MINUTE));
        }

        return output;
    }

    /**
     * Called when upvote button is clicked
     * Updates the upvotes of the tip in the database and updates the number of
     * upvotes shown on the screen
     *
     * @param view
     */
    public void upvoteClicked(View view) {
        if (tip.getCreator().equals(((Application) getApplicationContext()).getCurrentUser())) {
            Toast.makeText(getApplicationContext(), "You cannot upvote your own tip.", Toast.LENGTH_SHORT).show();
        } else {
            int oldVotes = tip.getUpvotes();
            int newVotes = oldVotes + 1;
            tip.setUpvotes(newVotes);
            tip.saveInBackground();
            upvoteView.setText(String.valueOf(newVotes));
        }
    }

    /**
     * Called when downvote button is clicked
     * Updates the downvotes of the tip in the database and updates the number of
     * downvotes shown on the screen
     *
     * @param view
     */
    public void downvoteClicked(View view) {
        if (tip.getCreator().equals(((Application) getApplicationContext()).getCurrentUser())) {
            Toast.makeText(getApplicationContext(), "You cannot downvote your own tip.", Toast.LENGTH_SHORT).show();
        } else {
            int oldVotes = tip.getDownvotes();
            int newVotes = oldVotes + 1;
            tip.setDownvotes(newVotes);
            tip.saveInBackground();
            downvoteView.setText(String.valueOf(newVotes));
        }
    }

    /**
     * Called when the location TextView or icon is clicked
     * Takes the user to Google Maps, which will show the calculated
     * route from user's current position to the position of the tip
     *
     * @param view
     */
    public void navigateClicked(View view) {
        ParseGeoPoint geoPoint = tip.getLocation();
        if (geoPoint != null) {
            String url = "http://maps.google.com/maps?daddr=" + geoPoint.getLatitude() + "," + geoPoint.getLongitude();
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "Cannot navigate to unknown location.", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Called when favourites button is clicked.
     * Adds the tip to the user's list of favourites.
     *
     * @param view
     */
    public void favouritesButtonClicked(View view) {
        TipperUser user = ((Application) getApplicationContext()).getCurrentUser();
        user.addFavourite(tip);
        user.saveInBackground();
        Toast.makeText(getApplicationContext(), "Tip added to favourites.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Used for navigation with back button on actionbar
     *
     * @return
     */
    @Override
    public Intent getSupportParentActivityIntent() {
        return getParentActivity();
    }

    /**
     * Used for navigation with back button on actionbar
     *
     * @return
     */
    @Override
    public Intent getParentActivityIntent() {
        return getParentActivity();
    }

    /**
     * Used for navigation with back button on actionbar
     * Specifies which Activity to go back to in which case
     * Note, in all cases, this back button behaves like the
     * hardware back button
     *
     * @return
     */
    private Intent getParentActivity() {
        Intent intent = null;
        if (sourceActivity.equals("MainActivity")) {
            intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else if (sourceActivity.equals("ShowGroupActivity")) {
            intent = new Intent(this, ShowGroupActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else if (sourceActivity.equals("ListActivity")) {
            intent = new Intent(this, ListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        return intent;
    }

    /**
     * Sets up the ShareActionProvider, making it possible to share a Tip by a click on the ActionBar
     * The Tip can then be shared to various apps, including Facebook and Google+
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_tip, menu);

        MenuItem item = menu.findItem(R.id.share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        shareActionProvider.setShareIntent(createShareIntent());
        shareActionProvider.setOnShareTargetSelectedListener(new ShareActionProvider.OnShareTargetSelectedListener() {
            @Override
            public boolean onShareTargetSelected(ShareActionProvider source, Intent intent) {
                final String packageName = intent.getComponent().getPackageName();
                if (packageName.contains("facebook")) {
                    shareTipOnFacebook();
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Creates default Intent for sharing Tip on other apps than Facebook
     * This sharing includes title, description of Tip and a link to the image
     * (only if the Tip has an image attached to it)
     * @return
     */
    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        String title = getIntent().getExtras().getString("title");
        String description = getIntent().getExtras().getString("description");
        String url = getIntent().getExtras().getString("imageUrl");

        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Tip for you: " + title);
        if(url != null) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Tip for you: \n" + title + "\n" + description + "\n " + url);
        } else {
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Tip for you: \n" + title + "\n" + description);
        }

        return shareIntent;
    }



    /**
     * Handles setting up the content of the Facebook ShareDialog and
     * FacebookCallBack (used for completing/cancelling a ShareDialog)
     */
    private void shareTipOnFacebook() {
        Profile profile = Profile.getCurrentProfile();
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (profile != null && accessToken != null) {
            // check if app has permission to publish
            if (accessToken.getPermissions().contains("publish_actions")) {

                // setup the post contents
                ShareLinkContent shareContent = new ShareLinkContent.Builder()
                        .setContentTitle(tip.getTitle())
                        .setContentDescription(tip.getDescription())
                        .setImageUrl(Uri.parse(tip.getImage().getUrl()))
                        .setContentUrl(Uri.parse(tip.getImage().getUrl()))
                        .build();

                // show the facebook sharing dialog
                ShareDialog shareDialog = new ShareDialog(ShowTipActivity.this);
                shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                    @Override
                    public void onSuccess(Sharer.Result result) {
                        Log.d(ACTIVITY_ID, "Tip shared on Facebook");
                    }

                    @Override
                    public void onCancel() {
                        Log.d(ACTIVITY_ID, "Facebook sharing cancelled");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(getApplicationContext(), "Sharing failed. Please check your internet connection.", Toast.LENGTH_LONG).show();
                        Log.e(ACTIVITY_ID, "Facebook sharing failed: " + exception.getMessage());
                        exception.printStackTrace();
                    }
                });


                if (ShareDialog.canShow(ShareLinkContent.class)) {
                    shareDialog.show(shareContent);
                }

            } else {
                // if app does not have sharing permission, request it
                List<String> permissions = Arrays.asList("publish_actions");
                LoginManager.getInstance().logInWithPublishPermissions(this, permissions);
            }
        }
    }

    /**
     * Handles clicks on menu items
     * In all cases where user is sent to another activity, an extra is added to the intent
     * to keep track of which activity we came from
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
        } else if (id == R.id.profile) {
            Intent intent = new Intent(this, MyProfileActivity.class);
            intent.putExtra("source", ACTIVITY_ID);
            startActivity(intent);
            return true;
        } else if (id == R.id.main_menu_logout) {
            TipperUser user = ((Application) getApplicationContext()).getCurrentUser();
            // disconnect from google api if user is google user
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
                // in all cases, unpin the user from the local datastore
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
        } else if (id == R.id.share) {

        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * methods below required only for use of GoogleApiClient, which is necessary for logout **
     */
    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
