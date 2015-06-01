package kstr14.tipper.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;

import com.parse.ParseException;

import kstr14.tipper.Application;
import kstr14.tipper.Data.TipperUser;
import kstr14.tipper.R;


public class MyProfileActivity extends ActionBarActivity {

    private static final String ACTIVITY_ID = "MyProfileActivity";

    private TextView usernameView;
    private TextView emailView;

    private TipperUser user;

    private String sourceActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sourceActivity = getIntent().getExtras().getString("source");

        user = ((Application)getApplicationContext()).getCurrentUser();

        // initialize UI elements
        usernameView = (TextView) findViewById(R.id.myProfile_tv_username);
        emailView = (TextView) findViewById(R.id.myProfile_tv_email);

        usernameView.setText(user.getUsername());
        emailView.setText(user.getEmail());
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
        if (sourceActivity.equals("CreateGroupActivity")) {
            intent = new Intent(this, CreateGroupActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else if (sourceActivity.equals("CreateTipActivity")) {
            intent = new Intent(this, CreateTipActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else if (sourceActivity.equals("MainActivity")) {
            intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else if (sourceActivity.equals("MyGroupsActivity")) {
            intent = new Intent(this, MyGroupsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else if (sourceActivity.equals("TipListActivity")) {
            intent = new Intent(this, TipListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else if (sourceActivity.equals("SearchTipActivity")) {
            intent = new Intent(this, SearchTipActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else if (sourceActivity.equals("ShowGroupActivity")) {
            intent = new Intent(this, ShowGroupActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else if (sourceActivity.equals("ShowTipActivity")) {
            intent = new Intent(this, ShowTipActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else {
            Log.d(ACTIVITY_ID, "No sourceActivity specified.");
        }
        return intent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_profile, menu);
        return true;
    }

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
            Intent intent = new Intent(this, TipListActivity.class);
            intent.putExtra("source", ACTIVITY_ID);
            intent.putExtra("context", "favourites");
            startActivity(intent);
            return true;
        }  else if (id == R.id.logout) {
            try {
                ((Application) getApplicationContext()).getCurrentUser().unpin();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ((Application) getApplicationContext()).setCurrentUser(null);
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
