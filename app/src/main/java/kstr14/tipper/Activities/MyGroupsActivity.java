package kstr14.tipper.Activities;

import android.annotation.TargetApi;
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

public class MyGroupsActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String ACTIVITY_ID = "MyGroupsActivity";

    public static final int CREATE_GROUP_REQUEST = 2;

    private List<Group> myGroups;
    private ListView listView;

    private List<String> allGroups;
    private Menu menu;

    private String sourceActivity;

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

        sourceActivity = getIntent().getExtras().getString("source");

        listView = (ListView) findViewById(R.id.myGroupsListView);

        TipperUser user = ((Application) getApplicationContext()).getCurrentUser();

        user.getGroups().getQuery().findInBackground(new FindCallback<Group>() {
            @Override
            public void done(List<Group> list, ParseException e) {
                myGroups = list;
                listView.setAdapter(new GroupBaseAdapter(getApplicationContext(), myGroups));
            }
        });

        // set click listener for groups in list to move to show group activity
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // grap selected group and start intent for show group activity
                Group group = (Group) listView.getAdapter().getItem(position);
                Intent intent = new Intent(MyGroupsActivity.this, ShowGroupActivity.class);
                intent.putExtra("source", ACTIVITY_ID);
                intent.putExtra("ID", group.getUuidString());
                startActivity(intent);

            }
        });

        // no groups to show
        TextView msg = (TextView) findViewById(R.id.myGroups_tv_empty);
        msg.setText("You are currently not a member of any groups.");
        listView.setEmptyView(msg);

        // load all group names into list to use for searchview
        ParseQuery<Group> query = ParseQuery.getQuery("Group");
        query.findInBackground(new FindCallback<Group>() {
            @Override
            public void done(List<Group> list, ParseException e) {
                allGroups = new ArrayList<String>();
                for (Group group : list) {
                    allGroups.add(group.getName().trim());
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateGroupList();
    }

    private void updateGroupList() {
        // update current user's group list
        TipperUser user = ((Application) getApplicationContext()).getCurrentUser();
        user.getGroups().getQuery().findInBackground(new FindCallback<Group>() {
            @Override
            public void done(List<Group> list, ParseException e) {
                if (e == null) {
                    myGroups = list;
                    listView.setAdapter(new GroupBaseAdapter(getApplicationContext(), myGroups));
                } else {
                    e.printStackTrace();
                }

            }
        });

        // update all group list (to update SearchView suggestions)
        ParseQuery<Group> query = ParseQuery.getQuery("Group");
        query.whereNotEqualTo("uuid", "dummy");
        query.findInBackground(new FindCallback<Group>() {
            @Override
            public void done(List<Group> list, ParseException e) {
                if (e == null) {
                    List<String> newAllGroups = new ArrayList<String>();
                    for(Group group : list) {
                        if(!group.getUuidString().equals("dummy")) {
                            newAllGroups.add(group.getName());
                        }
                    }
                    allGroups = newAllGroups;
                } else {
                    Log.d("item", "Error: " + e.getMessage());
                }
            }
        });

    }


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

                @Override
                public boolean onQueryTextSubmit(String s) {
                    Intent intent = new Intent(getApplicationContext(), TipListActivity.class);
                    intent.putExtra("source", ACTIVITY_ID);
                    intent.putExtra("query", s);
                    startActivity(intent);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String query) {
                    loadHistory(query);
                    return true;
                }
            });


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


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void loadHistory(String query) {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            // Cursor
            String[] columns = new String[] { "_id", "text" };
            Object[] temp = new Object[] { 0, "default" };

            MatrixCursor cursor = new MatrixCursor(columns);

            List<String> result = new ArrayList<>();

            for(int i = 0;  i < allGroups.size(); i++) {
                if(allGroups.get(i).toLowerCase().contains(query.toLowerCase())){
                    temp[0] = i;
                    temp[1] = allGroups.get(i);
                    cursor.addRow(temp);
                    result.add(allGroups.get(i));
                }
            }
            SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            final SearchView searchView = (SearchView) menu.findItem(R.id.main_menu_search).getActionView();
            searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
            searchView.setSuggestionsAdapter(new SearchViewAdapter(this, cursor, result));
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
        if (sourceActivity.equals("CreateGroupActivity")) {
            intent = new Intent(this, CreateGroupActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else if (sourceActivity.equals("CreateTipActivity")) {
            intent = new Intent(this, CreateTipActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else if (sourceActivity.equals("MainActivity")) {
            intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else if (sourceActivity.equals("MyGroupsActivity")) {
            intent = new Intent(this, MyGroupsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else if (sourceActivity.equals("TipListActivity")) {
            intent = new Intent(this, TipListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else if (sourceActivity.equals("SearchTipActivity")) {
            intent = new Intent(this, SearchTipActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else if (sourceActivity.equals("ShowGroupActivity")) {
            intent = new Intent(this, ShowGroupActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else if (sourceActivity.equals("ShowTipActivity")) {
            intent = new Intent(this, ShowTipActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else {
            Log.d(ACTIVITY_ID, "No sourceActivity specified.");
        }
        return intent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handling action bar events
        int id = item.getItemId();

        if (id == R.id.favourites) {
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
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

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
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}