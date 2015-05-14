package kstr14.tipper.Activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import kstr14.tipper.Adapters.GroupAdapter;
import kstr14.tipper.Data.Group;
import kstr14.tipper.R;


public class MyGroupsActivity extends ActionBarActivity {

    public static final int CREATE_GROUP_REQUEST = 2;

    private ListView listView;
    private GroupAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_groups);

        listView = (ListView) findViewById(R.id.myGroupsListView);
        adapter = new GroupAdapter(this);
        listView.setAdapter(adapter);

        // set click listener for groups in list to move to show group activity
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // grap selected group and start intent for show group activity
                Group group = (Group) listView.getAdapter().getItem(position);
                Intent intent = new Intent(MyGroupsActivity.this, ShowGroupActivity.class);
                intent.putExtra("ID", group.getUuidString());
                startActivity(intent);

            }
        });

        // no groups to show
        TextView msg = (TextView) findViewById(R.id.empty);
        msg.setText("You are currently not a member of any groups.");
        listView.setEmptyView(msg);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_groups, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
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
            Intent intent = new Intent(this, CreateGroupActivity.class);
            startActivityForResult(intent, CREATE_GROUP_REQUEST);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
