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
                    dateView.setText(tip.getStartDate().toString());
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
