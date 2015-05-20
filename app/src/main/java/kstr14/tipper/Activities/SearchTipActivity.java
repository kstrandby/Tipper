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

import kstr14.tipper.R;


public class SearchTipActivity extends ActionBarActivity {

    private EditText keywordsInput;
    private EditText locationInput;
    private SeekBar priceSeekBarInput;
    private CheckBox foodCheckBox;
    private CheckBox drinksCheckBox;
    private CheckBox otherCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_tip);

        // initialize UI elements
        keywordsInput = (EditText) findViewById(R.id.searchTip_ed_keywords);
        locationInput = (EditText) findViewById(R.id.searchTip_ed_location);
        priceSeekBarInput = (SeekBar) findViewById(R.id.searchTip_sb_price);
        foodCheckBox = (CheckBox) findViewById(R.id.searchTip_cb_food);
        drinksCheckBox = (CheckBox) findViewById(R.id.searchTip_cb_drinks);
        otherCheckBox = (CheckBox) findViewById(R.id.searchTip_cb_other);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_tip, menu);
        return true;
    }

    public void searchTip(View view) {
        Intent intent = new Intent(this, SearchResultActivity.class);
        intent.putExtra("source", "SearchTipActivity");
        intent.putExtra("searchType", "Tip");
        startActivity(intent);
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
