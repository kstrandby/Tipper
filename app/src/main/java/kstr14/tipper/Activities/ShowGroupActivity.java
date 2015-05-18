package kstr14.tipper.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.List;

import kstr14.tipper.Adapters.TipBaseAdapter;
import kstr14.tipper.Data.Group;
import kstr14.tipper.Data.Tip;
import kstr14.tipper.R;

public class ShowGroupActivity extends ActionBarActivity {

    private ImageView imageView;
    private ListView listView;
    private TextView descriptionView;
    private Group group;

    private TipBaseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_group);

        // initialize UI elements
        imageView = (ImageView) findViewById(R.id.showGroup_iv_groupImage);
        listView = (ListView) findViewById(R.id.showGroup_lv_groups);
        descriptionView = (TextView) findViewById(R.id.showGroup_tv_description);

        // fetch group object from database
        String ID = getIntent().getExtras().getString("ID");
        ParseQuery<Group> query = ParseQuery.getQuery("Group");
        query.whereEqualTo("uuid", ID);
        query.getFirstInBackground(new GetCallback<Group>() {
            @Override
            public void done(Group object, ParseException e) {
                if (e == null) {
                    group = object;

                    // set actionBar title to name of group
                    getSupportActionBar().setTitle(group.getName());
                    descriptionView.setText(group.getDescription());

                    // fetch tips of group and set up adapter and listview
                    group.getTips().getQuery().findInBackground(new FindCallback<Tip>() {
                        @Override
                        public void done(List<Tip> list, ParseException e) {
                            adapter = new TipBaseAdapter(getApplicationContext(), list);
                            listView.setAdapter(adapter);
                        }
                    });

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
        } else if (id == R.id.action_add) {
            Intent intent = new Intent(this, CreateTipActivity.class);
            intent.putExtra("source", "ShowGroupActivity");
        }

        return super.onOptionsItemSelected(item);
    }
}
