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

public class MainActivity extends ActionBarActivity implements OnItemClickListener, OnItemLongClickListener {

    private static final String ACTIVITY_ID = "MainActivity";
    public static final int CREATE_TIP_REQUEST = 1;

    private boolean exit = false;

    private TipperUser user;

    private ListView listView;
    private List<Tip> tips;
    private Adapter adapter;
    private ImageButton foodButton;
    private ImageButton drinksButton;
    private ImageButton otherButton;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String uuid = getIntent().getExtras().getString("uuid");
        System.out.println(uuid);
        ParseQuery<TipperUser> query = ParseQuery.getQuery("TipperUser");
        query.whereEqualTo("uuid", uuid);
        try {
            user = query.getFirst();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(user == null) {
            System.out.println("user is still null");
            setUser(((Application)getApplicationContext()).getCurrentUser());
        }

        // initialize UI elements
        listView = (ListView) findViewById(R.id.main_lv_tips);
        foodButton = (ImageButton) findViewById(R.id.main_ib_food);
        drinksButton = (ImageButton) findViewById(R.id.main_ib_drinks);
        otherButton = (ImageButton) findViewById(R.id.main_ib_other);

        // set images for imageButtons
        Bitmap img = ImageHelper.decodeBitmapFromResource(getResources(), R.drawable.food, 256, 256);
        foodButton.setImageBitmap(img);
        img = ImageHelper.decodeBitmapFromResource(getResources(), R.drawable.drinks, 256, 256);
        drinksButton.setImageBitmap(img);
        img = ImageHelper.decodeBitmapFromResource(getResources(), R.drawable.other, 256, 256);
        otherButton.setImageBitmap(img);

        progressDialog = ProgressDialog.show(this, "Loading", "Please wait while data is being loaded...");
        updateTipList();

        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
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

    @Override
    public void onResume() {
        super.onResume();
        updateTipList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handling action bar events
        int id = item.getItemId();

        if (id == R.id.groups) {
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
                if(list != null && !list.isEmpty()) {
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
                    if(e.getCode() == ParseException.CONNECTION_FAILED
                            || e.getCode() == ParseException.TIMEOUT) {
                        TextView msg = (TextView) findViewById(R.id.main_tv_empty);
                        msg.setText("Connection error.\nPlease make sure phone is connected to the internet.");
                        listView.setEmptyView(msg);
                    } else {
                        TextView msg = (TextView) findViewById(R.id.main_tv_empty);
                        msg.setText("Unknown error. \nPlease try again later or contact the owner of the app if the problem persists.");
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

    public void foodCategoryClicked(View view) {
        Intent intent = new Intent(getApplicationContext(), TipListActivity.class);
        intent.putExtra("source", ACTIVITY_ID);
        intent.putExtra("context", "category");
        intent.putExtra("category", "Food");
        startActivity(intent);
    }

    public void drinksCategoryClicked(View view) {
        Intent intent = new Intent(getApplicationContext(), TipListActivity.class);
        intent.putExtra("source", ACTIVITY_ID);
        intent.putExtra("context", "category");
        intent.putExtra("category", "Drinks");
        startActivity(intent);
    }

    public void otherCategoryClicked(View view) {
        Intent intent = new Intent(getApplicationContext(), TipListActivity.class);
        intent.putExtra("source", ACTIVITY_ID);
        intent.putExtra("context", "category");
        intent.putExtra("category", "Other");
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // grab the selected tip and start intent for show tip activity
        Tip tip = (Tip)listView.getAdapter().getItem(position);
        Intent intent = new Intent(MainActivity.this, ShowTipActivity.class);
        intent.putExtra("source", ACTIVITY_ID);
        intent.putExtra("ID", tip.getUuidString());
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        // check if user has the rights to delete the tip (if user is creator of tip or owner of group)
        final Tip tip = (Tip) listView.getAdapter().getItem(position);
        if (tip.getCreator().equals(user) || ((Group)tip.getGroup()).getCreator().equals(user)) {
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

    public void setUser(TipperUser currentUser) {
        // set current user
        if(user != null) {
            user = currentUser;
            Log.d(ACTIVITY_ID, user.getUsername() + " logged in");
        } else {
            Log.d(ACTIVITY_ID, "User is anonymous");
        }
    }
}
