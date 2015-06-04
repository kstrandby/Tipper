package kstr14.tipper.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
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
import kstr14.tipper.ErrorHandler;
import kstr14.tipper.R;

/**
 * This activity is a general list activity used for several purposes, only containing a ListView
 * The adapter of the ListView depends on what content the activity is used for, and it can be either
 * a TipBaseAdapter, if we are showing tips (showing Favourites list, search result or category result),
 * or a GroupBaseAdapter, if we are showing groups (resulting from a group search)
 */

public class ListActivity extends ActionBarActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String ACTIVITY_ID = "ListActivity";
    private static final int NUMBER_OF_CATEGORIES = 3;

    // static constants used to keep track of which type of list we are showing
    private static final int LIST_TYPE_FAVOURITES = 1;
    private static final int LIST_TYPE_CATEGORY = 2;
    private static final int LIST_TYPE_TIPS_SEARCH = 3;
    private static final int LIST_TYPE_GROUPS_SEARCH = 4;

    private int listType;

    private ListView listView;
    private TextView emptyView;
    private ProgressDialog progressDialog;

    private Adapter adapter;

    private String sourceActivity;
    private Intent intent;
    private TipperUser user;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        googleApiClient =  new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .build();
        googleApiClient.connect();

        progressDialog = ProgressDialog.show(this, "Loading", "Please wait while data is being loaded...");

        user = ((Application)getApplicationContext()).getCurrentUser();

        listView = (ListView) findViewById(R.id.searchResult_lv_result);
        emptyView = (TextView) findViewById(R.id.listActivity_empty_view);

        // check the source of the intent to know how to set up the list
        intent = getIntent();
        sourceActivity = intent.getExtras().getString("source");
        String context = intent.getExtras().getString("context");

        // if we are showing favourites list, we do not care which activity we came from, just show the favourites
        if(context != null) {
            if(context.equals("favourites")) {
                getSupportActionBar().setTitle("Favourites");
                listType = LIST_TYPE_FAVOURITES;
            }
        }

        // otherwise, we have several options
        if(sourceActivity.equals(MainActivity.ACTIVITY_ID)) {
            // this is the case when one of the category buttons is clicked on the main screen
            if(context.equals("category")) {
                String category = intent.getExtras().getString("category");
                getSupportActionBar().setTitle(category);
                listType = LIST_TYPE_CATEGORY;
            }
        } else if(sourceActivity.equals(MyGroupsActivity.ACTIVITY_ID)) {
            // this is the case when a search for groups has been made
            getSupportActionBar().setTitle("Search Result");
            listType = LIST_TYPE_GROUPS_SEARCH;
        } else if (sourceActivity.equals(SearchTipActivity.ACTIVITY_ID)) {
            // this is the case when a search for tips has been made
            getSupportActionBar().setTitle("Search Result");
            listType = LIST_TYPE_TIPS_SEARCH;
        }
        updateList();

        // set click listeners
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
    }

    /**
     * Fetches the data and updates the ListView with the data
     * Uses the listType variable to keep track of what type of
     * data to fetch and show
     */
    public void updateList() {
        if (listType == LIST_TYPE_TIPS_SEARCH) {
            // this case is the result of a search for tips, so a query matching the search restrictions
            // will be constructed first
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
                        progressDialog.dismiss();
                        if (e == null) {
                            if(results != null && !results.isEmpty()) {
                                adapter = new TipBaseAdapter(getApplicationContext(), results);
                                listView.setAdapter((ListAdapter) adapter);
                            } else {
                                Log.d(ACTIVITY_ID, "No results for search.");
                                emptyView.setText("No results for search.");
                            }
                        } else {
                            Log.d(ACTIVITY_ID, "Parse error: " + e.getStackTrace().toString());
                            ErrorHandler.showConnectionErrorAlert(ListActivity.this, getParentActivity());
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
                        progressDialog.dismiss();
                        if (e == null) {
                            if (results != null && !results.isEmpty()) {
                                adapter = new TipBaseAdapter(getApplicationContext(), results);
                                listView.setAdapter((ListAdapter) adapter);
                            } else {
                                Log.d(ACTIVITY_ID, "No results for search.");
                                emptyView.setText("No results for search.");
                            }
                        } else {
                            Log.d(ACTIVITY_ID, "Parse error: " + e.getStackTrace().toString());
                            ErrorHandler.showConnectionErrorAlert(ListActivity.this, getParentActivity());
                        }
                    }
                });
            } else {
                emptyView.setText("No results for search.");
            }
        } else if (listType == LIST_TYPE_GROUPS_SEARCH) {
            // only option from MyGroupsActivity to ListActivity is that a search for a group has been
            // performed, so an Extra has been set on the Intent providing the query of the search
            final String queryString = intent.getExtras().getString("query");
            ParseQuery<Group> query = ParseQuery.getQuery("Group");
            query.whereContains("lowerCaseName", queryString);
            query.whereNotEqualTo("uuid", "dummy");
            query.findInBackground(new FindCallback<Group>() {
                @Override
                public void done(List<Group> list, ParseException e) {
                    progressDialog.dismiss();
                    if (e == null) {
                        if(list != null && !list.isEmpty()) {
                            adapter = new GroupBaseAdapter(getApplicationContext(), list);
                            listView.setAdapter((ListAdapter) adapter);
                        } else {
                            Log.d(ACTIVITY_ID, "No groups exist matching the search query: " + queryString);
                            emptyView.setText("No groups found matching the query.");
                        }
                    } else {
                        Log.d(ACTIVITY_ID, "Parse error: " + e.getStackTrace().toString());
                        ErrorHandler.showConnectionErrorAlert(ListActivity.this, getParentActivity());
                    }
                }
            });
        } else if (listType == LIST_TYPE_FAVOURITES) {
            // just show the user's favourites list in this case
            ParseQuery<Tip> favouritesQuery = user.getFavourites().getQuery();
            favouritesQuery.findInBackground(new FindCallback<Tip>() {
                @Override
                public void done(List<Tip> favourites, ParseException e) {
                    progressDialog.dismiss();
                    if (e == null) {
                        if (favourites != null && !favourites.isEmpty()) {
                            adapter = new TipBaseAdapter(getApplicationContext(), favourites);
                            listView.setAdapter((ListAdapter) adapter);
                        } else {
                            Log.d(ACTIVITY_ID, "User's favourites list is empty.");
                            emptyView.setText("Your favourites list is currently empty.");
                        }
                    } else {
                        Log.d(ACTIVITY_ID, "Parse error: " + e.getStackTrace().toString());
                        ErrorHandler.showConnectionErrorAlert(ListActivity.this, getParentActivity());
                    }
                }
            });
        } else if (listType == LIST_TYPE_CATEGORY) {
            // this option from MainActivity to ListActivity corresponds to one of the category buttons
            // was clicked, and an Extra has been set on the Intent to provide which category
            final String category = intent.getExtras().getString("category");
            final ParseQuery<Tip> tipQuery = ParseQuery.getQuery("Tip");
            tipQuery.whereEqualTo("private", false);
            tipQuery.whereEqualTo("category", category);
            tipQuery.findInBackground(new FindCallback<Tip>() {
                public void done(List<Tip> itemList, ParseException e) {
                    progressDialog.dismiss();
                    if (e == null) {
                        if (itemList != null && !itemList.isEmpty()) {
                            adapter = new TipBaseAdapter(getApplicationContext(), itemList);
                            listView.setAdapter((ListAdapter) adapter);
                        } else {
                            Log.d(ACTIVITY_ID, "No tips in category: " + category);
                            emptyView.setText("No tips in category: " + category);
                        }
                    } else {
                        Log.d(ACTIVITY_ID, "Parse error: " + e.getStackTrace().toString());
                        ErrorHandler.showConnectionErrorAlert(ListActivity.this, getParentActivity());
                    }
                }
            });
        }
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
        int id = item.getItemId();

        if (id == R.id.groups) {
            Intent intent = new Intent(this, MyGroupsActivity.class);
            intent.putExtra("source", ACTIVITY_ID);
            startActivity(intent);
            return true;
        } else if (id == R.id.favourites) {
            Intent intent = new Intent(this, ListActivity.class);
            intent.putExtra("source", ACTIVITY_ID);
            intent.putExtra("context", "favourites");
            startActivity(intent);
            return true;
        } else if (id == R.id.profile) {
            Intent intent = new Intent(this, MyProfileActivity.class);
            intent.putExtra("source", ACTIVITY_ID);
            startActivity(intent);
            return true;
        } else if (id == R.id.main_menu_logout){
            if(user.isGoogleUser()) {
                Log.d(ACTIVITY_ID, "Google user signing out.....");
                if(googleApiClient.isConnected()) {
                    Plus.AccountApi.clearDefaultAccount(googleApiClient);
                    googleApiClient.disconnect();
                    Log.d(ACTIVITY_ID, "googleApiClient was connected, user is signed out now");
                } else {
                    Log.e(ACTIVITY_ID, "Trying to log out user, but GoogleApiClient was disconnected");
                }
            }
            try {
                ((Application)getApplicationContext()).getCurrentUser().unpin();
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
            }
            ((Application)getApplicationContext()).setCurrentUser(null);
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.about) {
            Intent intent = new Intent(this, AboutActivity.class);
            intent.putExtra("source", ACTIVITY_ID);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

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

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (adapter instanceof  GroupBaseAdapter) {
            final Group group = (Group) listView.getAdapter().getItem(position);
            if (group.getCreator().equals(user)) {
                // create dialog for deletion of item
                AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
                builder.setTitle("Remove tip?");
                builder.setMessage("Are you sure you wish to delete this group?");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // remove tip from database
                        group.deleteInBackground();
                        updateList();
                        Toast.makeText(getBaseContext(), "Group has been deleted.", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.create().show();
                return true;
            } else {
                Toast.makeText(getApplicationContext(), "You do not have the rights to delete this group.", Toast.LENGTH_SHORT).show();
                return true;
            }
        } else if (adapter instanceof  TipBaseAdapter) {
            //TODO check for favourites list - in this case longclick should just remove tip from favourites list
            // check if user has the rights to delete the tip (if user is creator of tip or owner of group)
            final Tip tip = (Tip) listView.getAdapter().getItem(position);
            if (tip.getCreator().equals(user) || ((Group)tip.getGroup()).getCreator().equals(user)) {
                // create dialog for deletion of item
                AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
                builder.setTitle("Remove tip?");
                builder.setMessage("Are you sure you wish to delete this tip?");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // remove tip from database
                        tip.deleteInBackground();
                        updateList();
                        Toast.makeText(getBaseContext(), "Tip has been deleted.", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.create().show();
                return true;
            } else {
                Toast.makeText(getApplicationContext(), "You do not have the rights to delete this tip.", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }

    /*** methods below required only for use of GoogleApiClient, which is necessary for logout ***/
    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
