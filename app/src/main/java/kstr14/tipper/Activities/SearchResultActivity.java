package kstr14.tipper.Activities;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.ListView;

import kstr14.tipper.Adapters.GroupAdapter;
import kstr14.tipper.R;
import kstr14.tipper.Adapters.TipAdapter;


public class SearchResultActivity extends ActionBarActivity {

    private ListView listView;
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        listView = (ListView) findViewById(R.id.searchResultListView);

        String searchType = getIntent().getExtras().getString("searchType");
        if(searchType.equals("Tip")) {
            // set tipadapter
            adapter = new TipAdapter(this);
            listView.setAdapter((android.widget.ListAdapter) adapter);

        } else if (searchType.equals("Group")) {
            // set group adapter
            adapter = new GroupAdapter(this);
            listView.setAdapter((android.widget.ListAdapter) adapter);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_result, menu);
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
