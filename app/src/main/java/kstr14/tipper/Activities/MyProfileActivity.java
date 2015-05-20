package kstr14.tipper.Activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import kstr14.tipper.Application;
import kstr14.tipper.Data.TipperUser;
import kstr14.tipper.R;


public class MyProfileActivity extends ActionBarActivity {

    private TextView usernameView;
    private TextView emailView;

    private TipperUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        user = ((Application)getApplicationContext()).getCurrentUser();

        // initialize UI elements
        usernameView = (TextView) findViewById(R.id.myProfile_tv_username);
        emailView = (TextView) findViewById(R.id.myProfile_tv_email);

        usernameView.setText(user.getUsername());
        emailView.setText(user.getEmail());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_profile, menu);
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
