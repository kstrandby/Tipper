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
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.List;

import kstr14.tipper.Adapters.TipBaseAdapter;
import kstr14.tipper.Application;
import kstr14.tipper.Data.Group;
import kstr14.tipper.Data.Tip;
import kstr14.tipper.Data.TipperUser;
import kstr14.tipper.ImageHelper;
import kstr14.tipper.R;

/**
 * MainActivity shows the main screen of the app, containing a list of public tips,
 * 3 buttons taking the user to lists of particular categories of tips (namely food, drinks or other)
 */
public class MainActivity extends ActionBarActivity
        implements OnItemClickListener, OnItemLongClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // ACTIVITY_ID is used for logging and keeping track of navigation between activities
    public static final String ACTIVITY_ID = "MainActivity";

    public static final int CREATE_TIP_REQUEST = 1;

    // UI elements
    private ListView listView;
    private List<Tip> tips;
    private Adapter adapter;
    private ImageButton foodButton;
    private ImageButton drinksButton;
    private ImageButton otherButton;
    private Bitmap foodBitmap;
    private Bitmap drinksBitmap;
    private Bitmap otherBitmap;
    private ProgressDialog progressDialog;

    private boolean exit = false; // keep track of back button clicks

    private TipperUser user;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set up google API for google log out
        googleApiClient =  new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .build();
        googleApiClient.connect();

        user = ((Application)getApplicationContext()).getCurrentUser();

        // initialize UI elements
        listView = (ListView) findViewById(R.id.main_lv_tips);
        foodButton = (ImageButton) findViewById(R.id.main_ib_food);
        drinksButton = (ImageButton) findViewById(R.id.main_ib_drinks);
        otherButton = (ImageButton) findViewById(R.id.main_ib_other);
        setBitmaps();

        // show ProgressDialog until tips have been downloaded
        progressDialog = ProgressDialog.show(this, "Loading", "Please wait while data is being loaded...");
        updateTipList();

        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
    }

    /**
     * Handles setting up bitmaps and recycling them to save memory
     */
    public void setBitmaps() {
        // set images for imageButtons
        if(foodBitmap != null && !foodBitmap.isRecycled()) {
            foodBitmap.recycle();
            foodBitmap = null;
        }
        foodBitmap = ImageHelper.decodeBitmapFromResource(getResources(), R.drawable.food, 256, 256);
        foodButton.setImageBitmap(foodBitmap);
        if(drinksBitmap != null && !drinksBitmap.isRecycled()) {
            drinksBitmap.recycle();
            drinksBitmap = null;
        }
        drinksBitmap = ImageHelper.decodeBitmapFromResource(getResources(), R.drawable.drinks, 256, 256);
        drinksButton.setImageBitmap(drinksBitmap);
        if(otherBitmap != null && !otherBitmap.isRecycled()) {
            otherBitmap.recycle();
            otherBitmap = null;
        }
        otherBitmap = ImageHelper.decodeBitmapFromResource(getResources(), R.drawable.other, 256, 256);
        otherButton.setImageBitmap(otherBitmap);
    }

    /**
     * Handles back button clicks - if user clicks back button twice, the app exits
     */
    @Override
    public void onBackPressed() {
        if(exit) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.toast_main_back_pressed, Toast.LENGTH_SHORT).show();
            exit = true;
        }
    }

    /**
     * When this activity resumes, refresh the list of tips and set the bitmaps again
     */
    @Override
    public void onResume() {
        super.onResume();
        setBitmaps();
        updateTipList();
    }

    /**
     * Creates the ActionBar menu
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Handles menu item clicks
     * @param item, the menu item clicked
     * @return
     */
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
        } else if (id == R.id.main_action_add_tip) {
            Intent intent = new Intent(this, CreateTipActivity.class);
            intent.putExtra("source", ACTIVITY_ID);
            startActivityForResult(intent, CREATE_TIP_REQUEST);
            return true;
        } else if (id == R.id.main_menu_search) {
            Intent intent = new Intent(getApplicationContext(), SearchTipActivity.class);
            intent.putExtra("source", ACTIVITY_ID);
            startActivity(intent);
            return true;
        } else if (id == R.id.main_menu_logout){
            if(user != null) {
                if(user.isGoogleUser()) {
                    Log.d(ACTIVITY_ID, "Google user signing out.....");

                    if(googleApiClient.isConnected()) {
                        Plus.AccountApi.clearDefaultAccount(googleApiClient);
                        googleApiClient.disconnect();
                        Log.d(ACTIVITY_ID, "googleApiClient was connected, user is signed out now");
                    } else {
                        Log.d(ACTIVITY_ID, "googleApiClient was disconnected");
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

    /**
     * refreshes the list of tips by pulling data fro the database
     */
    private void updateTipList() {

        ParseQuery<Tip> query = ParseQuery.getQuery("Tip");
        query.whereEqualTo("private", false);
        query.findInBackground(new FindCallback<Tip>() {
            @Override
            public void done(List<Tip> list, ParseException e) {
                progressDialog.dismiss();
                if (list != null && !list.isEmpty()) {
                    tips = list;
                    adapter = new TipBaseAdapter(getApplicationContext(), tips);
                    listView.setAdapter((ListAdapter) adapter);
                } else if (e == null) {
                    // means there is just no tips in database
                    TextView msg = (TextView) findViewById(R.id.main_tv_empty);
                    msg.setText("Currently no tips to show.");
                    listView.setEmptyView(msg);
                    Log.d(ACTIVITY_ID, "Did not find any public tips");
                } else if (e != null) {
                    // means connection error
                    if (e.getCode() == ParseException.CONNECTION_FAILED
                            || e.getCode() == ParseException.TIMEOUT) {
                        TextView msg = (TextView) findViewById(R.id.main_tv_empty);
                        msg.setText("Connection error.\n" +
                                "Please make sure phone is connected to the internet.");
                        listView.setEmptyView(msg);
                    } else {
                        TextView msg = (TextView) findViewById(R.id.main_tv_empty);
                        msg.setText("Unknown error. \n" +
                                "Please try again later or contact the owner of the app " +
                                "if the problem persists.");
                        listView.setEmptyView(msg);
                    }
                }
            }
        });
    }

    /**
     * Called when CreateTipActivity finished
     * Calls the updateTipList to update the list with the new tip
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(requestCode == CREATE_TIP_REQUEST) {
            if(resultCode == RESULT_OK) {
                updateTipList();
            }
        }
    }

    /**
     * Called when the Food category button is clicked
     * Takes the user to the TipList screen
     * @param view
     */
    public void foodCategoryClicked(View view) {
        Intent intent = new Intent(getApplicationContext(), ListActivity.class);
        intent.putExtra("source", ACTIVITY_ID);
        intent.putExtra("context", "category");
        intent.putExtra("category", "Food");
        startActivity(intent);
    }

    /**
     * Called when the Drinks category button is clicked
     * Takes the user to the TipList screen
     * @param view
     */
    public void drinksCategoryClicked(View view) {
        Intent intent = new Intent(getApplicationContext(), ListActivity.class);
        intent.putExtra("source", ACTIVITY_ID);
        intent.putExtra("context", "category");
        intent.putExtra("category", "Drinks");
        startActivity(intent);
    }

    /**
     * Called when the Other category button is clicked
     * Takes the user to the TipList screen
     * @param view
     */
    public void otherCategoryClicked(View view) {
        Intent intent = new Intent(getApplicationContext(), ListActivity.class);
        intent.putExtra("source", ACTIVITY_ID);
        intent.putExtra("context", "category");
        intent.putExtra("category", "Other");
        startActivity(intent);
    }

    /**
     * Handles clicks on items in ListView
     * Takes the user to the ShowTipActivity to show the details of the tip clicked
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // grab the selected tip and start intent for show tip activity
        Tip tip = (Tip)listView.getAdapter().getItem(position);
        Intent intent = new Intent(MainActivity.this, ShowTipActivity.class);
        intent.putExtra("source", ACTIVITY_ID);
        intent.putExtra("title", tip.getTitle());
        intent.putExtra("description", tip.getDescription());
        if(tip.getImage() != null) {
            intent.putExtra("imageUrl", tip.getImage().getUrl());
        }
        intent.putExtra("ID", tip.getUuidString());
        startActivity(intent);
    }

    /**
     * Handles long clicks on items in the ListView
     * Long click deletes the tip clicked after checking if the user is allowed to delete the tip
     * and prompting for confirmation via an AlertDialog
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        // check if user has the rights to delete the tip (if user is creator of tip or owner of group)
        final Tip tip = (Tip) listView.getAdapter().getItem(position);
        TipperUser groupCreator = null;
        try {
            groupCreator = (TipperUser) ((Group)tip.getGroup()).getCreator();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (tip.getCreator().equals(user) || groupCreator.equals(user)) {
            // create dialog for deletion of item
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
    }

    /**
     * Sets the global user variable
     * @param currentUser
     */
    public void setUser(TipperUser currentUser) {
        if(user != null) {
            user = currentUser;
            Log.d(ACTIVITY_ID, user.getUsername() + " logged in");
        } else {
            Log.d(ACTIVITY_ID, "User is anonymous");
        }
    }

    /**
     * Recycles the bitmaps onStop() to be memory efficient
     */
    @Override
    public void onStop() {
        super.onStop();
        if(foodBitmap != null) {
            foodBitmap.recycle();
            foodBitmap = null;
        }
        if(drinksBitmap != null) {
            drinksBitmap.recycle();
            drinksBitmap = null;
        }
        if(otherBitmap != null) {
            otherBitmap.recycle();
            otherBitmap = null;
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
