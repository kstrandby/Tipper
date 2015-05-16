package kstr14.tipper.Activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import kstr14.tipper.Data.Tip;
import kstr14.tipper.R;

//TODO show PRICE as well in screen!!


public class ShowTipActivity extends ActionBarActivity {

    private Tip tip;
    private ImageView imageView;
    private TextView upvoteView;
    private TextView downvoteView;
    private TextView descriptionView;
    private TextView dateView;
    private TextView locationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tip);

        // initialize all UI elements
        imageView = (ImageView) findViewById(R.id.image);
        upvoteView = (TextView) findViewById(R.id.upvotes);
        downvoteView = (TextView) findViewById(R.id.downvotes);
        descriptionView = (TextView) findViewById(R.id.descriptionView);
        dateView = (TextView) findViewById(R.id.dateView);
        locationView = (TextView) findViewById(R.id.locationView);

        String ID = getIntent().getExtras().getString("ID");
        ParseQuery<Tip> query = ParseQuery.getQuery("Tip");
        query.whereEqualTo("uuid", ID);

        query.getFirstInBackground(new GetCallback<Tip>() {

            @Override
            public void done(Tip object, ParseException e) {
                if (e == null) {
                    tip = object;
                    String title = tip.getTitle();
                    System.out.println(title);
                    getSupportActionBar().setTitle(title);
                    upvoteView.setText(String.valueOf(tip.getUpvotes()));
                    downvoteView.setText(String.valueOf(tip.getDownvotes()));
                    descriptionView.setText(tip.getDescription());
                    dateView.setText(tip.getStartDate().toString());

                    //TODO set image and location
                    locationView.setText("Location unknown");

                    // note to self: if no image, set image to either food, drinks or other, according to the category of the tip

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
