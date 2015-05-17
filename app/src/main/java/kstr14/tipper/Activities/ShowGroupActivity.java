package kstr14.tipper.Activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import kstr14.tipper.Data.Group;
import kstr14.tipper.R;

public class ShowGroupActivity extends ActionBarActivity {

    private ImageView imageView;
    private ListView listView;
    private Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_group);

        // initialize UI elements
        imageView = (ImageView) findViewById(R.id.showGroup_iv_groupImage);
        listView = (ListView) findViewById(R.id.showGroup_lv_groups);

        String ID = getIntent().getExtras().getString("ID");
        ParseQuery<Group> query = ParseQuery.getQuery("Group");
        query.whereEqualTo("uuid", ID);

        query.getFirstInBackground(new GetCallback<Group>() {

            @Override
            public void done(Group object, ParseException e) {
                if (e == null) {
                    group = object;
                    String name = group.getName();
                    System.out.println(name);
                    getSupportActionBar().setTitle(name);

                    //TODO set image

                } else {
                    e.printStackTrace();
                }
            }

        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_group, menu);
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
