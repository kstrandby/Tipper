package kstr14.tipper.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.parse.ParseException;

import kstr14.tipper.Adapters.TipAdapter;
import kstr14.tipper.Application;
import kstr14.tipper.BitmapHelper;
import kstr14.tipper.Data.Tip;
import kstr14.tipper.Data.TipperUser;
import kstr14.tipper.R;


public class MainActivity extends ActionBarActivity {

    public static final int CREATE_TIP_REQUEST = 1;

    private ListView listView;
    private TipAdapter adapter;
    private ImageButton foodButton;
    private ImageButton drinksButton;
    private ImageButton otherButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TipperUser user = ((Application)getApplicationContext()).getCurrentUser();
        System.out.println(user.getUsername() + " logged in");

        // initialize UI elements
        listView = (ListView) findViewById(R.id.main_lv_tips);
        foodButton = (ImageButton) findViewById(R.id.main_ib_food);
        drinksButton = (ImageButton) findViewById(R.id.main_ib_drinks);
        otherButton = (ImageButton) findViewById(R.id.main_ib_other);

        // set list adapter
        adapter = new TipAdapter(this);
        listView.setAdapter(adapter);

        // set images for imageButtons
        Bitmap img = BitmapHelper.decodeBitmapFromResource(getResources(), R.drawable.food, 256, 256);
        foodButton.setImageBitmap(img);
        img = BitmapHelper.decodeBitmapFromResource(getResources(), R.drawable.drinks, 256, 256);
        drinksButton.setImageBitmap(img);
        img = BitmapHelper.decodeBitmapFromResource(getResources(), R.drawable.other, 256, 256);
        otherButton.setImageBitmap(img);

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

    public void foodCategoryClicked(View view) {
        Intent intent = new Intent(getApplicationContext(), SearchResultActivity.class);
        intent.putExtra("source", "MainActivity");
        intent.putExtra("category", "food");
        startActivity(intent);
    }

    public void drinksCategoryClicked(View view) {
        Intent intent = new Intent(getApplicationContext(), SearchResultActivity.class);
        intent.putExtra("source", "MainActivity");
        intent.putExtra("category", "drinks");
        startActivity(intent);
    }

    public void otherCategoryClicked(View view) {
        Intent intent = new Intent(getApplicationContext(), SearchResultActivity.class);
        intent.putExtra("source", "MainActivity");
        intent.putExtra("category", "other");
        startActivity(intent);
    }
}
