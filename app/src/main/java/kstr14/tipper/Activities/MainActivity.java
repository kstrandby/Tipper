package kstr14.tipper.Activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.parse.ParseUser;

import kstr14.tipper.Data.Tip;
import kstr14.tipper.R;
import kstr14.tipper.Adapters.TipAdapter;


public class MainActivity extends ActionBarActivity {

    public static final int CREATE_TIP_REQUEST = 1;

    private ListView listView;
    private TipAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView);
        adapter = new TipAdapter(this);
        listView.setAdapter(adapter);

        // set click listener for tips in list to move to show tip activity
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // grab the selected tip and start intent for show tip activity
                Tip tip = (Tip)listView.getAdapter().getItem(position);
                Intent intent = new Intent(MainActivity.this, ShowTipActivity.class);
                intent.putExtra("ID", tip.getUuidString());
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handling action bar events
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.groups) {
            Intent intent = new Intent(this, MyGroupsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.profile) {
            Intent intent = new Intent(this, MyProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_add) {
            Intent intent = new Intent(this, CreateTipActivity.class);
            startActivityForResult(intent, CREATE_TIP_REQUEST);
            return true;
        } else if (id == R.id.action_search) {
            Intent intent = new Intent(getApplicationContext(), SearchTipActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.logout){
            ParseUser.logOut();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateTipList() {
        adapter.loadObjects();
        listView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(requestCode == CREATE_TIP_REQUEST) {
            if(resultCode == RESULT_OK) {
                updateTipList();
            }
        }
    }
}
