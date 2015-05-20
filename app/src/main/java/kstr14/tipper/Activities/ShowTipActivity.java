package kstr14.tipper.Activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.Calendar;
import java.util.Locale;

import kstr14.tipper.BitmapHelper;
import kstr14.tipper.Data.Tip;
import kstr14.tipper.R;

public class ShowTipActivity extends ActionBarActivity {

    private Tip tip;
    private ImageView imageView;
    private ImageButton upvoteButton;
    private ImageButton downvoteButton;
    private ImageButton favouritesButton;
    private TextView upvoteView;
    private TextView downvoteView;
    private TextView descriptionView;
    private ImageView dateIcon;
    private ImageView locationIcon;
    private ImageView priceIcon;
    private TextView dateView;
    private TextView locationView;
    private TextView priceView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tip);

        // initialize all UI elements
        imageView = (ImageView) findViewById(R.id.showTip_iv_tipImage);
        upvoteButton = (ImageButton) findViewById(R.id.showTip_ib_upvote);
        downvoteButton = (ImageButton) findViewById(R.id.showTip_ib_downvote);
        favouritesButton = (ImageButton) findViewById(R.id.showTip_ib_favourites);
        upvoteView = (TextView) findViewById(R.id.showTip_tv_upvotes);
        downvoteView = (TextView) findViewById(R.id.showTip_tv_downvotes);
        descriptionView = (TextView) findViewById(R.id.showTip_tv_description);
        dateIcon = (ImageView) findViewById(R.id.showTip_iv_dateIcon);
        locationIcon = (ImageView) findViewById(R.id.showTip_iv_locationIcon);
        priceIcon = (ImageView) findViewById(R.id.showTip_iv_priceIcon);
        dateView = (TextView) findViewById(R.id.showTip_tv_date);
        locationView = (TextView) findViewById(R.id.showTip_tv_location);
        priceView = (TextView) findViewById(R.id.showTip_tv_price);

        // set images of ImageButtons and ImageViews
        Bitmap img = BitmapHelper.decodeBitmapFromResource(getResources(), R.drawable.thumbs_up, 128, 128);
        upvoteButton.setImageBitmap(Bitmap.createScaledBitmap(img, 64, 64, false));
        img = BitmapHelper.decodeBitmapFromResource(getResources(), R.drawable.thumbs_down, 128, 128);
        downvoteButton.setImageBitmap(Bitmap.createScaledBitmap(img, 64, 64, false));
        img = BitmapHelper.decodeBitmapFromResource(getResources(), R.drawable.star, 128, 128);
        favouritesButton.setImageBitmap(Bitmap.createScaledBitmap(img, 64, 64, false));
        img = BitmapHelper.decodeBitmapFromResource(getResources(), R.drawable.ic_action_go_to_today, 128, 128);
        dateIcon.setImageBitmap(Bitmap.createScaledBitmap(img, 64, 64, false));
        img = BitmapHelper.decodeBitmapFromResource(getResources(), R.drawable.ic_action_map, 128, 128);
        locationIcon.setImageBitmap(Bitmap.createScaledBitmap(img, 64, 64, false));

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
                    //TODO set image and location
                    locationView.setText("Location unknown");

                    // note to self: if no image, set image to either food, drinks or other, according to the category of the tip

                    Bitmap img = BitmapHelper.decodeBitmapFromResource(getResources(), R.drawable.food, 256, 256);
                    imageView.setImageBitmap(img);

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_tip, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
