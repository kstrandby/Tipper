package kstr14.tipper.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import kstr14.tipper.Adapters.GroupBaseAdapter;
import kstr14.tipper.Adapters.TipBaseAdapter;
import kstr14.tipper.Application;
import kstr14.tipper.Data.Group;
import kstr14.tipper.Data.Tip;
import kstr14.tipper.Data.TipperUser;
import kstr14.tipper.R;


public class TipListActivity extends ActionBarActivity {

    private static final String ACTIVITY_ID = "TipListActivity";
    private static final int NUMBER_OF_CATEGORIES = 3;

    private ListView listView;
    private Adapter adapter;

    private String sourceActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = (ListView) findViewById(R.id.searchResult_lv_result);

        // check the source of the intent to know how to set up adapter
        Intent intent = getIntent();
        sourceActivity = intent.getExtras().getString("source");

        if(sourceActivity.equals("MainActivity")) {
            String context = intent.getExtras().getString("context");
            if(context.equals("category")) {
                // this option from MainActivity to TipListActivity corresponds to one of the category buttons
                // was clicked, and an Extra has been set on the Intent to provide which category
                final String category = intent.getExtras().getString("category");
                final ParseQuery<Tip> tipQuery = ParseQuery.getQuery("Tip");
                getSupportActionBar().setTitle(category);
                tipQuery.whereEqualTo("private", false);
                tipQuery.whereEqualTo("category", category);
                tipQuery.findInBackground(new FindCallback<Tip>() {
                    public void done(List<Tip> itemList, ParseException e) {
                        if (e == null) {
                            if (itemList != null && !itemList.isEmpty()) {
                                adapter = new TipBaseAdapter(getApplicationContext(), itemList);
                                listView.setAdapter((ListAdapter) adapter);
                            } else {
                                Log.d(ACTIVITY_ID, "No tips in category: " + category);
                                //TODO show message on screen
                            }
                        } else {
                            Log.d(ACTIVITY_ID, "Parse error: " + e.getMessage());
                        }
                    }
                });
            } else if (context.equals("favourites")) {
                // favourites was chosen from menu
                getSupportActionBar().setTitle("Favourites");
                TipperUser user = ((Application)getApplicationContext()).getCurrentUser();
                ParseQuery<Tip> favouritesQuery = user.getFavourites().getQuery();
                favouritesQuery.findInBackground(new FindCallback<Tip>() {
                    @Override
                    public void done(List<Tip> favourites, ParseException e) {
                        if (e == null) {
                            if (favourites != null && !favourites.isEmpty()) {
                                adapter = new TipBaseAdapter(getApplicationContext(), favourites);
                                listView.setAdapter((ListAdapter) adapter);
                            } else {
                                Log.d(ACTIVITY_ID, "User's favourites list is empty.");
                                //TODO show message on screen
                            }
                        } else {
                            Log.d(ACTIVITY_ID, "Parse error: " + e.getMessage());
                        }
                    }
                });
            }
        } else if(sourceActivity.equals("MyGroupsActivity")) {
            // only option from MyGroupsActivity to TipListActivity is that a search for a group has been
            // performed, so an Extra has been set on the Intent providing the query of the search
            getSupportActionBar().setTitle("Search Result");
            // Use this query to fetch the relevant groups
            final String queryString = intent.getExtras().getString("query");
            ParseQuery<Group> query = ParseQuery.getQuery("Group");
            query.whereContains("lowerCaseName", queryString);
            query.findInBackground(new FindCallback<Group>() {
                @Override
                public void done(List<Group> list, ParseException e) {
                    if (e == null) {
                        if(list != null && !list.isEmpty()) {
                            adapter = new GroupBaseAdapter(getApplicationContext(), list);
                            listView.setAdapter((ListAdapter) adapter);
                        } else {
                            Log.d(ACTIVITY_ID, "No groups exist matching the search query: " + queryString);
                        }
                    } else {
                        Log.d(ACTIVITY_ID, "Parse error: " + e.getMessage());
                    }
                }
            });
        } else if (sourceActivity.equals("SearchTipActivity")) {
            getSupportActionBar().setTitle("Search Result");
            String keyword = getIntent().getExtras().getString("keyword");
            String location = getIntent().getExtras().getString("location");
            String[] categories = getIntent().getExtras().getStringArray("categories");
            int maxPrice = getIntent().getExtras().getInt("maxPrice");

            if(categories.length > 1 && categories.length < NUMBER_OF_CATEGORIES) {
                // we have to fetch only tips with the corresponding categories
                // create query for each chosen category and merge them together
                List<ParseQuery<Tip>> queries = new ArrayList<>();
                for(String category : categories) {
                    ParseQuery<Tip> tipQuery = ParseQuery.getQuery("Tip");
                    tipQuery.whereEqualTo("category", category);
                    queries.add(tipQuery);
                }
                // now merge the queries to one and add the other constraints
                ParseQuery<Tip> mainQuery = ParseQuery.or(queries);
                mainQuery.whereEqualTo("private", false);
                mainQuery.whereContains("title", keyword);
                mainQuery.whereLessThanOrEqualTo("price", maxPrice);
                mainQuery.findInBackground(new FindCallback<Tip>() {
                    public void done(List<Tip> results, ParseException e) {
                        if (e == null) {
                            if(results != null && !results.isEmpty()) {
                                adapter = new TipBaseAdapter(getApplicationContext(), results);
                                listView.setAdapter((ListAdapter) adapter);
                            } else {
                                Log.d(ACTIVITY_ID, "No results for search.");
                                //TODO set message showing no tips found
                            }
                        } else {
                            Log.d(ACTIVITY_ID, "Parse error: " + e.getMessage());
                        }
                    }
                });
            } else if (categories.length == 1 || categories.length == NUMBER_OF_CATEGORIES){
                // we only specify one category for the query or no categories (if all categories have been chosen)
                ParseQuery<Tip> query = ParseQuery.getQuery("Tip");
                query.whereEqualTo("private", false);
                if (categories.length == 1) {
                    query.whereEqualTo("category", categories[0]);
                }
                query.whereContains("lowerCaseTitle", keyword.toLowerCase());
                query.whereLessThanOrEqualTo("price", maxPrice);
                query.findInBackground(new FindCallback<Tip>() {
                    @Override
                    public void done(List<Tip> results, ParseException e) {
                        if (e == null) {
                           if (results != null && !results.isEmpty()) {
                              adapter = new TipBaseAdapter(getApplicationContext(), results);
                               listView.setAdapter((ListAdapter) adapter);
                           } else {
                               Log.d(ACTIVITY_ID, "No results for search.");
                               //TODO set message showing no tips found
                           }
                        }
                    }
                });
            } else {
                //TODO set message showing no tips found
            }
        }

        // set items in list clickable
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter instanceof  GroupBaseAdapter) {
                    Group group = (Group) listView.getAdapter().getItem(position);
                    Intent intent = new Intent(getApplicationContext(), ShowGroupActivity.class);
                    intent.putExtra("source", ACTIVITY_ID);
                    intent.putExtra("ID", group.getUuidString());
                    startActivity(intent);
                } else if (adapter instanceof TipBaseAdapter) {
                    Tip tip = (Tip) listView.getAdapter().getItem(position);
                    Intent intent = new Intent(getApplicationContext(), ShowTipActivity.class);
                    intent.putExtra("source", ACTIVITY_ID);
                    intent.putExtra("ID", tip.getUuidString());
                    startActivity(intent);
                }
            }
        });
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
        if (sourceActivity.equals("MainActivity")) {
            intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else if (sourceActivity.equals("MyGroupsActivity")) {
            intent = new Intent(this, MyGroupsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else if (sourceActivity.equals("SearchTipActivity")) {
            intent = new Intent(this, SearchTipActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else {
            Log.d(ACTIVITY_ID, "No sourceActivity specified.");
        }
        return intent;
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
        // handling action bar events
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.groups) {
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
        } else if (id == R.id.profile) {
            Intent intent = new Intent(this, MyProfileActivity.class);
            intent.putExtra("source", ACTIVITY_ID);
            startActivity(intent);
            return true;
        } else if (id == R.id.logout){
            try {
                ((Application)getApplicationContext()).getCurrentUser().unpin();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ((Application)getApplicationContext()).setCurrentUser(null);
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
