package kstr14.tipper.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseQuery;

import java.util.List;

import kstr14.tipper.Adapters.TipBaseAdapter;
import kstr14.tipper.Application;
import kstr14.tipper.Data.Group;
import kstr14.tipper.Data.Tip;
import kstr14.tipper.Data.TipperUser;
import kstr14.tipper.ErrorHandler;
import kstr14.tipper.ImageHelper;
import kstr14.tipper.R;

/**
 * Activity showing details about a group
 * The activity shows the name and description of the group along with the image and
 * a list of the tips in the group
 * In addition, the activity uses two fragments to handle whether to show a Join group button
 * or a Leave group button, according to whether the user is a member of the group or not
 */
public class ShowGroupActivity extends ActionBarActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // ACTIVITY_ID is used for logging and keeping track of navigation between activities
    public static final String ACTIVITY_ID = "ShowGroupActivity";

    private Menu menu;

    // UI elements
    private ParseImageView imageView;
    private ListView listView;
    private TextView descriptionView;
    private ProgressDialog progressDialog;

    private Group group;
    private TipperUser currentUser;
    private TipBaseAdapter adapter;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_group);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = ProgressDialog.show(this, "Loading", "Please wait while Group is being loaded...");

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .build();
        googleApiClient.connect();

        // initialize UI elements
        imageView = (ParseImageView) findViewById(R.id.showGroup_iv_groupImage);
        listView = (ListView) findViewById(R.id.showGroup_lv_groups);
        descriptionView = (TextView) findViewById(R.id.showGroup_tv_description);

        currentUser = ((Application) getApplicationContext()).getCurrentUser();
        String groupID = getIntent().getExtras().getString("ID");

        // fetch group object from database
        ParseQuery<Group> groupQuery = ParseQuery.getQuery("Group");
        groupQuery.whereEqualTo("uuid", groupID);
        groupQuery.getFirstInBackground(new GetCallback<Group>() {
            @Override
            public void done(Group object, ParseException e) {
                progressDialog.dismiss();
                if (e == null && object != null) {
                    // check if user is member of group to show correct button (Leave/Join)
                    group = object;
                    group.getUsers().getQuery().findInBackground(new FindCallback<TipperUser>() {
                        @Override
                        public void done(List<TipperUser> list, ParseException e) {
                            if (e == null && list != null) {
                                if (list.contains(currentUser)) {
                                    // user member of group - show fragment with Leave button
                                    GroupMemberFragment memberFragment = new GroupMemberFragment();
                                    getFragmentManager().beginTransaction()
                                            .add(R.id.ShowGroupActivity_fragment_container, memberFragment).commit();
                                    // and show the add tip button on actionbar
                                    MenuItem addItem = menu.findItem(R.id.action_add_tip_to_group);
                                    addItem.setVisible(true);
                                } else {
                                    // user not member of group - show fragment with Join button
                                    GroupNotMemberFragment notMemberFragment = new GroupNotMemberFragment();
                                    getFragmentManager().beginTransaction()
                                            .add(R.id.ShowGroupActivity_fragment_container, notMemberFragment).commit();
                                    // and hide the add tip button on actionbar
                                    MenuItem addItem = menu.findItem(R.id.action_add_tip_to_group);
                                    addItem.setVisible(false);
                                    // hide list of tips if group is closed
                                    if (group.isClosed()) listView.setVisibility(View.INVISIBLE);
                                }
                            } else {
                                Log.e(ACTIVITY_ID, "Parse error: " + e.getMessage());
                                e.printStackTrace();
                                ErrorHandler.showConnectionErrorAlert(ShowGroupActivity.this, getParentActivity());
                            }
                        }
                    });
                    group = object;

                    // set actionBar title to name of group
                    getSupportActionBar().setTitle(group.getName());
                    descriptionView.setText(group.getDescription());

                    // set default image if no image exist in database
                    ParseFile image = group.getImage();
                    Bitmap img = ImageHelper.decodeBitmapFromResource(getResources(), R.drawable.ic_action_group_big, 256, 256);
                    if (image == null) {
                        imageView.setImageBitmap(img);
                    } else {
                        imageView.setPlaceholder(getResources().getDrawable(R.drawable.food));
                        imageView.setParseFile(image);
                        imageView.loadInBackground();
                    }

                    // fetch tips of group
                    updateTipList();
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            // grab the selected tip and start intent for show tip activity
                            Tip tip = (Tip) listView.getAdapter().getItem(position);
                            Intent intent = new Intent(ShowGroupActivity.this, ShowTipActivity.class);
                            intent.putExtra("source", ACTIVITY_ID);
                            intent.putExtra("ID", tip.getUuidString());
                            startActivity(intent);
                        }
                    });
                    listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                            // check if user has the rights to delete the tip (if user is creator of tip or owner of group)
                            final Tip tip = (Tip) listView.getAdapter().getItem(position);
                            try {
                                if (tip.getCreator().equals(currentUser) || group.getCreator().equals(currentUser)) {
                                    // create dialog for deletion of item
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ShowGroupActivity.this);
                                    builder.setTitle("Remove tip?");
                                    builder.setMessage("Are you sure you wish to delete this tip?");
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // remove tip from database

                                            tip.deleteInBackground();
                                            updateTipList();
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
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return true;
                        }
                    });
                } else {
                    Log.e(ACTIVITY_ID, "Parse error: " + e.getMessage());
                    e.printStackTrace();
                    ErrorHandler.showConnectionErrorAlert(ShowGroupActivity.this, getParentActivity());
                }
            }
        });
    }

    /**
     * Called when JOIN button is clicked
     * Adds the user to the group and the group to the user
     * @param view
     */
    public void joinGroup(View view) {
        group.addUser(currentUser);
        group.saveInBackground();
        currentUser.addGroup(group);
        currentUser.saveInBackground();
        // refresh the screen with the MemberFragment
        GroupMemberFragment memberFragment = new GroupMemberFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.ShowGroupActivity_fragment_container, memberFragment).commit();
        // set add button on action bar visible and list of tips
        MenuItem addItem = menu.findItem(R.id.action_add_tip_to_group);
        addItem.setVisible(true);
        listView.setVisibility(View.VISIBLE);
    }

    /**
     * Called when LEAVE button is clicked
     * Removes the user from the group and the group from the user
     * @param view
     */
    public void leaveGroup(View view) {
        group.removeUser(currentUser);
        group.saveInBackground();
        currentUser.removeGroup(group);
        currentUser.saveInBackground();
        // refresh the screen with the notMemberFragment
        GroupNotMemberFragment notMemberFragment = new GroupNotMemberFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.ShowGroupActivity_fragment_container, notMemberFragment).commit();
        // check if the group is closed, so we need to hide the tips now the user is not member
        if (group.isClosed()) {
            listView.setVisibility(View.INVISIBLE);
        }
        MenuItem addItem = menu.findItem(R.id.action_add_tip_to_group);
        addItem.setVisible(false);
    }

    /**
     * Updates the list of tips by fetching the tips from the database
     */
    public void updateTipList() {
        // fetch tips of group
        group.getTips().getQuery().findInBackground(new FindCallback<Tip>() {
            @Override
            public void done(List<Tip> list, ParseException e) {
                if (e == null && list != null) {
                    adapter = new TipBaseAdapter(getApplicationContext(), list);
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e(ACTIVITY_ID, "Parse error: " + e.getMessage());
                    e.printStackTrace();
                    ErrorHandler.showConnectionErrorAlert(ShowGroupActivity.this, getParentActivity());
                }
            }
        });
    }

    /**
     * Handles back button clicks on ActionBar
     *
     * @return
     */
    @Override
    public Intent getSupportParentActivityIntent() {
        return getParentActivity();
    }

    /**
     * Handles back button clicks on ActionBar
     *
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
     * Creates the ActionBar menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_show_group, menu);
        return true;
    }

    /**
     * Handles ActionBar click events
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_tip_to_group) {
            Intent intent = new Intent(ShowGroupActivity.this, CreateTipActivity.class);
            intent.putExtra("source", ACTIVITY_ID);
            intent.putExtra("groupID", group.getUuidString());
            startActivityForResult(intent, MainActivity.CREATE_TIP_REQUEST);
            return true;
        } else if (id == R.id.groups) {
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
        } else if (id == R.id.main_menu_logout) {
            TipperUser user = ((Application) getApplicationContext()).getCurrentUser();
            if (user.isGoogleUser()) {
                Log.d(ACTIVITY_ID, "Google user signing out.....");
                if (googleApiClient.isConnected()) {
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
                ((Application) getApplicationContext()).getCurrentUser().unpin();
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
            }
            ((Application) getApplicationContext()).setCurrentUser(null);
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

    /**
     * Called when returned from creating a tip
     * Updates the list of tips so that the newly created tip is shown
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == MainActivity.CREATE_TIP_REQUEST) {
            if (resultCode == RESULT_OK) {
                updateTipList();
            }
        }
    }

    /**
     * methods below required only for use of GoogleApiClient, which is necessary for logout **
     */
    @Override
    public void onConnected(Bundle bundle) { }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { }
}
