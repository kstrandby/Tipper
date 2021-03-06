package kstr14.tipper.Activities;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import kstr14.tipper.Adapters.GroupBaseAdapter;
import kstr14.tipper.Adapters.SearchViewAdapter;
import kstr14.tipper.Application;
import kstr14.tipper.Data.Group;
import kstr14.tipper.Data.TipperUser;
import kstr14.tipper.R;

/**
 * Activity showing ListView of current user's groups
 * Contains a SearchView on the ActionBar allowing the user to search for groups by name
 */
public class MyGroupsActivity extends ActionBarActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // ACTIVITY_ID is used for logging and keeping track of navigation between activities
    public static final String ACTIVITY_ID = "MyGroupsActivity";

    public static final int CREATE_GROUP_REQUEST = 2;

    // UI elements
    private ListView listView;
    private ProgressDialog progressDialog;

    private List<String> allGroups;
    private List<Group> myGroups;

    private Menu menu;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_groups);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        googleApiClient =  new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .build();
        googleApiClient.connect();

        // set up UI elements
        listView = (ListView) findViewById(R.id.myGroupsListView);
        progressDialog = ProgressDialog.show(this, "Loading", "Please wait while data is being loaded...");
        updateGroupList();

        // set click listener for groups in list to move to ShowGroupActivity
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // grab selected group and start intent for ShowGroupActivity
                Group group = (Group) listView.getAdapter().getItem(position);
                Intent intent = new Intent(MyGroupsActivity.this, ShowGroupActivity.class);
                intent.putExtra("source", ACTIVITY_ID);
                intent.putExtra("ID", group.getUuidString());
                startActivity(intent);
            }
        });
    }

    /**
     * Updates the list when activity is resumed
     */
    @Override
    public void onResume() {
        super.onResume();
        updateGroupList();
    }

    /**
     * Updates the list of groups by fetching data from the database
     */
    private void updateGroupList() {
        // update current user's group list
        TipperUser user = ((Application) getApplicationContext()).getCurrentUser();
        if(user != null) {
            user.getGroups().getQuery().findInBackground(new FindCallback<Group>() {
                @Override
                public void done(List<Group> list, ParseException e) {
                    progressDialog.dismiss();
                    if (list != null && !list.isEmpty()) {
                        myGroups = list;
                        listView.setAdapter(new GroupBaseAdapter(getApplicationContext(), myGroups));
                    } else if (e == null) {
                        // means that the user is not member of any groups
                        TextView msg = (TextView) findViewById(R.id.myGroups_tv_empty);
                        msg.setText("You are currently non member of any groups.");
                        listView.setEmptyView(msg);
                        Log.d(ACTIVITY_ID, "Did not find any groups.");
                    } else if (e != null) {
                        // means connection error
                        TextView msg = (TextView) findViewById(R.id.myGroups_tv_empty);
                        msg.setText("Connection error.\n" +
                                "Please make sure phone is connected to the internet.");
                        listView.setEmptyView(msg);
                        Log.e(ACTIVITY_ID, "Parse error: " + e.getMessage());
                        e.printStackTrace();
                    } else {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.e(ACTIVITY_ID, "User object is null");
        }

        // load all group names into list to use for SearchView
        ParseQuery<Group> query = ParseQuery.getQuery("Group");
        query.whereNotEqualTo("uuid", "dummy");
        query.findInBackground(new FindCallback<Group>() {
            @Override
            public void done(List<Group> list, ParseException e) {
                if (list != null && !list.isEmpty()) {
                    if (e == null) {
                        allGroups = new ArrayList<>();
                        for (Group group : list) {
                            allGroups.add(group.getName().trim());
                        }
                    } else {
                        Log.e(ACTIVITY_ID, "Parse error: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    Log.e(ACTIVITY_ID, "Could not fetch any groups for suggestion list.");
                }
            }
        });
    }

    /**
     * Creates the ActionBar menu as well as sets up the SearchView on the ActionBar
     * @param menu
     * @return
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_groups, menu);
        this.menu = menu;

        // Associate searchable configuration with the SearchView
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) menu.findItem(R.id.main_menu_search).getActionView();
            searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                // go to ListActivity (showing search result) upon query submit
                @Override
                public boolean onQueryTextSubmit(String s) {
                    Intent intent = new Intent(getApplicationContext(), ListActivity.class);
                    intent.putExtra("source", ACTIVITY_ID);
                    intent.putExtra("query", s);
                    startActivity(intent);
                    return true;
                }

                // updates the query suggestions upon text change
                @Override
                public boolean onQueryTextChange(String query) {
                    loadHistory(query);
                    return true;
                }
            });

            // NOTE: this does not work - clicks are not being registered for some reason!
            searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                @Override
                public boolean onSuggestionSelect(int i) {
                    System.out.println("Selected " + i);
                    return true;
                }

                @Override
                public boolean onSuggestionClick(int i) {
                    System.out.println("Clicked " + i);
                    return true;
                }
            });
        }
        return true;
    }

    /**
     * Loads suggestions into the suggestion list to show when user types in SearchView
     * @param query
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void loadHistory(String query) {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            String[] columns = new String[] { "_id", "text" };
            Object[] temp = new Object[] { 0, "default" };

            MatrixCursor cursor = new MatrixCursor(columns);
            List<String> result = new ArrayList<>();

            // match the entered text with groups
            for(int i = 0;  i < allGroups.size(); i++) {
                if(allGroups.get(i).toLowerCase().contains(query.toLowerCase())){
                    temp[0] = i;
                    temp[1] = allGroups.get(i);
                    cursor.addRow(temp);
                    result.add(allGroups.get(i));
                }
            }

            // set the suggestions
            SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            final SearchView searchView = (SearchView) menu.findItem(R.id.main_menu_search).getActionView();
            searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
            searchView.setSuggestionsAdapter(new SearchViewAdapter(this, cursor, result));
        }
    }

    /**
     * Handles back button clicks on ActionBar
     * @return
     */
    @Override
    public Intent getSupportParentActivityIntent() {
        return getParentActivity();
    }

    /**
     * Handles back button clicks on ActionBar
     * @return
     */
    @Override
    public Intent getParentActivityIntent() {
        return getParentActivity();
    }

    /**
     * Handles back button clicks on ActionBar
     * Behaves identically to hardware back button clicks
     * @return
     */
    private Intent getParentActivity() {
        onBackPressed();
        return null;
    }

    /**
     * Handles menu item clicks
     * @param item, the menu item clicked
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handling action bar events
        int id = item.getItemId();
        if (id == R.id.favourites) {
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
            TipperUser user = ((Application)getApplicationContext()).getCurrentUser();
            if(user.isGoogleUser()) {
                Log.d(ACTIVITY_ID, "Google user signing out.....");
                if(googleApiClient.isConnected()) {
                    Plus.AccountApi.clearDefaultAccount(googleApiClient);
                    googleApiClient.disconnect();
                    Log.d(ACTIVITY_ID, "googleApiClient was connected, user is signed out now");
                } else {
                    Log.e(ACTIVITY_ID, "Trying to log out user, but GoogleApiClient was disconnected");
                }
            } else if(user.isFacebookUser()) {
                Log.d(ACTIVITY_ID, "Facebook user signing out......");
                FacebookSdk.sdkInitialize(getApplicationContext());
                LoginManager.getInstance().logOut();
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
        } else if (id == R.id.my_groups_menu_add_group) {
            Intent intent = new Intent(this, CreateGroupActivity.class);
            intent.putExtra("source", ACTIVITY_ID);
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

    /**
     * Called upon return from creation of group
     * Updates the group list to fetch the new item
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(requestCode == CREATE_GROUP_REQUEST) {
            if(resultCode == RESULT_OK) {
                updateGroupList();
            }
        }
    }

    /*** methods below required only for use of GoogleApiClient, which is necessary for logout ***/
    @Override
    public void onConnected(Bundle bundle) { }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { }
}