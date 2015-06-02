package kstr14.tipper.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseImageView;
import com.parse.ParseQuery;

import java.util.Calendar;
import java.util.Locale;

import kstr14.tipper.Application;
import kstr14.tipper.Data.Tip;
import kstr14.tipper.Data.TipperUser;
import kstr14.tipper.ImageHelper;
import kstr14.tipper.MapsHelper;
import kstr14.tipper.R;

public class ShowTipActivity extends ActionBarActivity {

    private static final String ACTIVITY_ID = "ShowTipActivity";

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

    private String sourceActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tip);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        Bitmap img = ImageHelper.decodeBitmapFromResource(getResources(), R.drawable.thumbs_up, 128, 128);
        upvoteButton.setImageBitmap(Bitmap.createScaledBitmap(img, 64, 64, false));
        img = ImageHelper.decodeBitmapFromResource(getResources(), R.drawable.thumbs_down, 128, 128);
        downvoteButton.setImageBitmap(Bitmap.createScaledBitmap(img, 64, 64, false));
        img = ImageHelper.decodeBitmapFromResource(getResources(), R.drawable.star, 128, 128);
        favouritesButton.setImageBitmap(Bitmap.createScaledBitmap(img, 64, 64, false));

        sourceActivity = getIntent().getExtras().getString("source");

        String ID = getIntent().getExtras().getString("ID");
        ParseQuery<Tip> query = ParseQuery.getQuery("Tip");
        query.whereEqualTo("uuid", ID);

        query.getFirstInBackground(new GetCallback<Tip>() {
            @Override
            public void done(Tip object, ParseException e) {
                if (e == null) {
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
                    if(geoPoint == null) {
                        locationView.setText("Location unknown");
                    } else {
                        LatLng latLng = MapsHelper.getLatLngFromParseGeoPoint(geoPoint);
                        String address = MapsHelper.getAddressFromLatLng(latLng, getApplicationContext());
                        locationView.setText(address);
                    }


                    // set default image if no image exist in database
                    ParseFile image = tip.getImage();
                    Bitmap img = ImageHelper.decodeBitmapFromResource(getResources(), R.drawable.food, 256, 256);
                    if(image == null) {
                        imageView.setImageBitmap(img);
                    } else {
                        imageView.setPlaceholder(getResources().getDrawable(R.drawable.food));
                        imageView.setParseFile(image);
                        imageView.loadInBackground();
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    private String prettyOutputDates() {
        Calendar start = Calendar.getInstance();
        start.setTime(tip.getStartDate());
        Calendar end = Calendar.getInstance();
        end.setTime(tip.getEndDate());
        String output = "";
        if(start.get(Calendar.YEAR) == end.get(Calendar.YEAR)
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
                    + end.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US)
                    + String.format("%02d", end.get(Calendar.DAY_OF_MONTH)) + "-"
                    + String.format("%02d", end.get(Calendar.MONTH) + 1) + "-"
                    + end.get(Calendar.YEAR) + " "
                    + String.format("%02d", end.get(Calendar.HOUR_OF_DAY)) + ":"
                    + String.format("%02d", end.get(Calendar.MINUTE));
        }

        return output;
    }

    public void upvoteClicked(View view) {
        if(tip.getCreator().equals(((Application)getApplicationContext()).getCurrentUser())) {
            Toast.makeText(getApplicationContext(), "You cannot upvote your own tip.", Toast.LENGTH_SHORT).show();
        } else {
            int oldVotes = tip.getUpvotes();
            int newVotes = oldVotes + 1;
            tip.setUpvotes(newVotes);
            tip.saveInBackground();
            upvoteView.setText(String.valueOf(newVotes));
        }
    }

    public void downvoteClicked(View view) {
        if(tip.getCreator().equals(((Application)getApplicationContext()).getCurrentUser())) {
            Toast.makeText(getApplicationContext(), "You cannot downvote your own tip.", Toast.LENGTH_SHORT).show();
        } else {
            int oldVotes = tip.getDownvotes();
            int newVotes = oldVotes + 1;
            tip.setDownvotes(newVotes);
            tip.saveInBackground();
            downvoteView.setText(String.valueOf(newVotes));
        }
    }

    public void navigateClicked(View view) {
        ParseGeoPoint geoPoint = tip.getLocation();
        if(geoPoint != null) {
            String url = "http://maps.google.com/maps?daddr=" + geoPoint.getLatitude() + "," + geoPoint.getLongitude();
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,  Uri.parse(url));
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "Cannot navigate to unknown location.", Toast.LENGTH_SHORT).show();
        }

    }

    public void favouritesButtonClicked(View view) {
        TipperUser user = ((Application) getApplicationContext()).getCurrentUser();
        user.addFavourite(tip);
        user.saveInBackground();
        Toast.makeText(getApplicationContext(), "Tip added to favourites.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        return getParentActivity();
    }

    @Override
    public Intent getParentActivityIntent() {
        return getParentActivity();
    }

    private Intent getParentActivity() {
        Intent intent = null;
        if (sourceActivity.equals("MainActivity")) {
            intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else if(sourceActivity.equals("ShowGroupActivity")) {
            intent = new Intent(this, ShowGroupActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else if(sourceActivity.equals("TipListActivity")) {
            intent = new Intent(this, TipListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        return intent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_tip, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.groups) {
            Intent intent = new Intent(this, MyGroupsActivity.class);
            intent.putExtra("source", ACTIVITY_ID);
            startActivity(intent);
            return true;
        } else if (id == R.id.favourites) {
            Intent intent = new Intent(this, TipListActivity.class);
            intent.putExtra("source", ACTIVITY_ID);
            intent.putExtra("context", "favourites");
            startActivity(intent);
            return true;
        } else if (id == R.id.profile) {
            Intent intent = new Intent(this, MyProfileActivity.class);
            intent.putExtra("source", ACTIVITY_ID);
            startActivity(intent);
            return true;
        } else if (id == R.id.main_menu_logout){
            try {
                ((Application)getApplicationContext()).getCurrentUser().unpin();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ((Application)getApplicationContext()).setCurrentUser(null);
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
