package kstr14.tipper.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

import kstr14.tipper.Adapters.TipBaseAdapter;
import kstr14.tipper.Data.Category;
import kstr14.tipper.Data.Tip;
import kstr14.tipper.R;


public class SearchResultActivity extends ActionBarActivity {

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        listView = (ListView) findViewById(R.id.searchResult_lv_result);

        // check the source of the intent to know how to set up adapter
        Intent intent = getIntent();
        String source = intent.getExtras().getString("source");
        if(source.equals("MainActivity")) {

            final String category = intent.getExtras().getString("category");

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Category");
            query.whereEqualTo("name", category);
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> itemList, ParseException e) {
                    if (e == null) {
                        // now get all the tips of that category
                        Category catObject = (Category) itemList.get(0);
                        catObject.getTips().getQuery().findInBackground(new FindCallback<Tip>() {
                            public void done(List<Tip> itemList, ParseException e) {
                                if (e == null) {
                                    TipBaseAdapter adapter = new TipBaseAdapter(getApplicationContext(), itemList);
                                    listView.setAdapter(adapter);
                                } else {
                                    Log.d("item", "Error: " + e.getMessage());
                                }
                            }
                        });


                    } else {
                        Log.d("item", "Error: " + e.getMessage());
                    }
                }
            });
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
