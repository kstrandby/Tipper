package kstr14.tipper.Activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import kstr14.tipper.Application;
import kstr14.tipper.Data.Category;
import kstr14.tipper.Data.Group;
import kstr14.tipper.Data.Tip;
import kstr14.tipper.Data.TipperUser;
import kstr14.tipper.ParseHelper;
import kstr14.tipper.R;

// TODO: check source of Intent and if source is ShowGroupActivity, set the owner group of the tip to the group
// also check if its a private group - and thereby a private tip

public class CreateTipActivity extends ActionBarActivity {

    private EditText titleInput;
    private EditText descriptionInput;
    private CheckBox foodCheckBox;
    private CheckBox drinksCheckBox;
    private CheckBox otherCheckBox;
    private SeekBar priceSeekBar;
    private TextView priceView;
    private LinearLayout startDateLayout;
    private LinearLayout endDateLayout;
    private TextView startDateView;
    private TextView startTimeView;
    private TextView endDateView;
    private TextView endTimeView;
    private Button createButton;
    private DatePickerDialog datePicker;
    private TimePickerDialog timePicker;
    private Spinner groupChoice;
    private Spinner repeatStyle;

    private Calendar current;
    private Calendar chosenStartDate;
    private Calendar chosenEndDate;
    private int chosenPrice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_tip);

        // set current date to correct timezone
        TimeZone timeZone = TimeZone.getTimeZone("Australia/Melbourne");
        current = Calendar.getInstance(timeZone);
        chosenStartDate = Calendar.getInstance();
        chosenEndDate = Calendar.getInstance();

        // initialize all UI elements
        titleInput = (EditText) findViewById(R.id.createTip_ed_title);
        descriptionInput = (EditText) findViewById(R.id.createTip_ed_description);
        foodCheckBox = (CheckBox) findViewById(R.id.createTip_cb_foodCategory);
        drinksCheckBox = (CheckBox) findViewById(R.id.createTip_cb_drinksCategory);
        otherCheckBox = (CheckBox) findViewById(R.id.createTip_cb_otherCategory);
        priceSeekBar = (SeekBar) findViewById(R.id.createTip_sb_price);
        priceView = (TextView) findViewById(R.id.createTip_tv_price);
        startDateLayout = (LinearLayout) findViewById(R.id.createTip_ll_startDate);
        endDateLayout = (LinearLayout) findViewById(R.id.createTip_ll_endDate);
        startDateView = (TextView) findViewById(R.id.createTip_tv_startDate);
        startTimeView = (TextView) findViewById(R.id.createTip_tv_startTime);
        endDateView = (TextView) findViewById(R.id.createTip_tv_endDate);
        endTimeView = (TextView) findViewById(R.id.createTip_tv_endTime);
        createButton = (Button) findViewById(R.id.createTip_b_create);
        groupChoice = (Spinner) findViewById(R.id.createTip_s_groupChoice);
        repeatStyle = (Spinner) findViewById(R.id.createTip_s_repeatStyle);

        // prevent EditText boxes to resize themselves when user enters a lot of text
        titleInput.setMaxLines(1);
        descriptionInput.setMaxLines(1);

        // set todays date as default date on start and end date textviews
        updateDateView(current, startDateView);
        updateDateView(current, endDateView);
        updateTimeView(current, startTimeView);
        updateTimeView(current, endTimeView);

        // populate the repeatStyle spinner with choices
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this,
                R.array.repeatstyles_array, R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        repeatStyle.setAdapter(arrayAdapter);

        // populate the groupchoice spinner with choices
        List<String> groupArray =  new ArrayList<>();
        groupArray.add("No group");
        try {
            TipperUser user = ((Application) getApplicationContext()).getCurrentUser();
            for(Group group: ParseHelper.getUsersGroups(user)) {
                groupArray.add(group.getName());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, R.layout.simple_spinner_item, groupArray);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);;
        groupChoice.setAdapter(adapter);

        // handle price seekbar changes
        priceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                priceView.setText("Price: " + progress + " AUD");
                chosenPrice = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // handle start and end date clicks
        startDateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateTimePicker(chosenStartDate, startDateView, startTimeView);
            }
        });

        endDateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker(chosenEndDate, endDateView, endTimeView);
            }
        });
    }

    public void showDateTimePicker(final Calendar calendar, final TextView dateView, final TextView timeView) {
        showDatePickerDialog(calendar, dateView, timeView);
    }

    public void showDatePickerDialog(final Calendar calendar, final TextView dateView, final TextView timeView) {
        datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(year, monthOfYear, dayOfMonth);
                showTimePickerDialog(calendar, timeView);
                updateDateView(calendar, dateView);
            }
        }, current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    public void showTimePickerDialog(final Calendar calendar, final TextView timeView) {
        timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                updateTimeView(calendar, timeView);
            }
        }, current.get(Calendar.HOUR_OF_DAY), current.get(Calendar.MINUTE), false);
        timePicker.show();
    }

    public void updateDateView(Calendar calendar, TextView dateView) {
        dateView.setText(String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)) + "-"
                + String.format("%02d", calendar.get(Calendar.MONTH) + 1) + "-"
                + calendar.get(Calendar.YEAR));
    }

    public void updateTimeView(Calendar calendar, TextView timeView){
        timeView.setText(String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":"
                + String.format("%02d", calendar.get(Calendar.MINUTE)));
    }

    /**
     * method called when CREATE button clicked
     */
    public void createTip(View view) {
        // fetch values
        String title = titleInput.getText().toString();
        String description = descriptionInput.getText().toString();

        //TODO fetch values from spinners
        String selectedRepeatStyle = repeatStyle.getSelectedItem().toString();
        if (selectedRepeatStyle.equals("One time event")) {

        } else if (selectedRepeatStyle.equals("Daily")) {

        } else if (selectedRepeatStyle.equals("Weekly")) {

        } else if (selectedRepeatStyle.equals("Monthly")) {

        } else if (selectedRepeatStyle.equals("Yearly")) {

        }
        String selectedGroup = groupChoice.getSelectedItem().toString();
        Group group = null;
        try {
            group = ParseHelper.getGroup(selectedGroup);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        final Tip tip = new Tip();
        String category = null;

        // input validation
        if(!foodCheckBox.isChecked() && !drinksCheckBox.isChecked() && !otherCheckBox.isChecked()) {
            Toast.makeText(getBaseContext(), "Please choose a category.", Toast.LENGTH_LONG).show();
        } else if(!validateTitle(title)) {
            Toast.makeText(getBaseContext(), "Please input a title.", Toast.LENGTH_SHORT).show();
        } else if (chosenStartDate.before(current)) {
            Toast.makeText(getBaseContext(), "Please input a valid start date.", Toast.LENGTH_SHORT).show();
        } else if (chosenEndDate.before(chosenStartDate)) {
            Toast.makeText(getBaseContext(), "End date cannot be before start date.", Toast.LENGTH_SHORT).show();
        } else {
            if (foodCheckBox.isChecked()) {
                category = "food";
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Category");
                query.whereEqualTo("name", category);
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> itemList, ParseException e) {
                        if (e == null) {
                            tip.addCategory((Category) itemList.get(0));
                        } else {
                            Log.d("item", "Error: " + e.getMessage());
                        }
                    }
                });
            }

            if (drinksCheckBox.isChecked()) {
                category = "drinks";
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Category");
                query.whereEqualTo("name", category);
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> itemList, ParseException e) {
                        if (e == null) {
                            tip.addCategory((Category) itemList.get(0));
                        } else {
                            Log.d("item", "Error: " + e.getMessage());
                        }
                    }
                });
            }
            if (otherCheckBox.isChecked()) {
                category = "other";
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Category");
                query.whereEqualTo("name", category);
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> itemList, ParseException e) {
                        if (e == null) {
                            tip.addCategory((Category) itemList.get(0));
                        } else {
                            Log.d("item", "Error: " + e.getMessage());
                        }
                    }
                });
            }

            tip.setTitle(title);
            tip.setDescription(description);
            tip.setDownvotes(0);
            tip.setUpvotes(0);
            tip.setPrice(chosenPrice);
            tip.setEndDate(chosenEndDate.getTime());
            tip.setStartDate(chosenStartDate.getTime());
            tip.setUuidString();
            final String finalCategory = category;
            final Group finalGroup = group;
            tip.saveInBackground(new SaveCallback() {

                @Override
                public void done(ParseException e) {
                    if (e == null) {

                        // and now, make the relation from category to tip
                        ParseQuery<ParseObject> query = ParseQuery.getQuery("Category");
                        query.whereEqualTo("name", finalCategory);
                        query.findInBackground(new FindCallback<ParseObject>() {
                            public void done(List<ParseObject> itemList, ParseException e) {
                                if (e == null) {
                                    ((Category) itemList.get(0)).addTip(tip);
                                    ((Category) itemList.get(0)).saveInBackground();
                                } else {
                                    Log.d("item", "Error: " + e.getMessage());
                                }
                            }
                        });
                        // finally, add tip to group
                        if(finalGroup != null) {
                            finalGroup.addTip(tip);
                        }
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Error saving: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

            });
        }
    }

    public boolean validateTitle(String title) {
        if(title.length() != 0) return true;
        else return false;
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_tip, menu);
        return true;
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
