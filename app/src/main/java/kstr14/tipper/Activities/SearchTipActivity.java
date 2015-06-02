package kstr14.tipper.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

import kstr14.tipper.Application;
import kstr14.tipper.R;


public class SearchTipActivity extends ActionBarActivity {

    private static final String ACTIVITY_ID = "SearchTipActivity";

    private EditText keywordInput;
    private EditText locationInput;
    private SeekBar priceSeekBarInput;
    private TextView priceView;
    private CheckBox foodCheckBox;
    private CheckBox drinksCheckBox;
    private CheckBox otherCheckBox;
    private int chosenPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_tip);

        // initialize UI elements
        keywordInput = (EditText) findViewById(R.id.searchTip_ed_keywords);
        locationInput = (EditText) findViewById(R.id.searchTip_ed_location);
        priceSeekBarInput = (SeekBar) findViewById(R.id.searchTip_sb_price);
        priceView = (TextView) findViewById(R.id.searchTip_tv_price);
        foodCheckBox = (CheckBox) findViewById(R.id.searchTip_cb_food);
        drinksCheckBox = (CheckBox) findViewById(R.id.searchTip_cb_drinks);
        otherCheckBox = (CheckBox) findViewById(R.id.searchTip_cb_other);

        // handle price SeekBar changes
        priceSeekBarInput.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                priceView.setText("Price: " + progress + " AUD");
                chosenPrice = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_tip, menu);
        return true;
    }

    public void searchTip(View view) {
        // Preparing extras for the Intent
        String keyword = keywordInput.getText().toString();
        String location = locationInput.getText().toString();
        List<String> categories = new ArrayList<>();
        if(foodCheckBox.isChecked()) categories.add("food");
        if(drinksCheckBox.isChecked()) categories.add("drinks");
        if(otherCheckBox.isChecked()) categories.add("other");
        String[] categoriesArray = categories.toArray(new String[categories.size()]);

        Intent intent = new Intent(this, TipListActivity.class);
        intent.putExtra("source", ACTIVITY_ID);
        intent.putExtra("keyword", keyword);
        intent.putExtra("location", location);
        intent.putExtra("categories", categoriesArray);
        intent.putExtra("maxPrice", chosenPrice);

        startActivity(intent);
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
}
