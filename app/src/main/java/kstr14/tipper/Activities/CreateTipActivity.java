package kstr14.tipper.Activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import kstr14.tipper.Data.Category;
import kstr14.tipper.Data.Tip;
import kstr14.tipper.R;


public class CreateTipActivity extends ActionBarActivity {

    private EditText titleInput;
    private EditText descriptionInput;
    private CheckBox foodCheckBox;
    private CheckBox drinksCheckBox;
    private CheckBox otherCheckBox;
    private SeekBar priceSeekBar;
    private TextView priceView;
    private TextView startDateView;
    private TextView endDateView;
    private Button createButton;
    private DatePickerDialog datePicker;
    private TimePickerDialog timePicker;
    private LinearLayout repeatLinearLayout;

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
        startDateView = (TextView) findViewById(R.id.createTip_tv_startDate);
        endDateView = (TextView) findViewById(R.id.createTip_tv_endDate);
        createButton = (Button) findViewById(R.id.createTip_b_create);

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
        startDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDateView.setText("Start: ");
                showDateTimePicker(chosenStartDate, startDateView);
            }
        });

        endDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endDateView.setText("End: \t\t");
                showDateTimePicker(chosenEndDate, endDateView);
            }
        });



    }

    public void onStart() {
        super.onStart();
        repeatLinearLayout = (LinearLayout) findViewById(R.id.repeatLinearLayout);
        repeatLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Repeat")
                        .setMessage("")
                        .show();
            }
        });
        }



    public void showDateTimePicker(final Calendar calendar, final TextView view) {
        showDatePickerDialog(calendar, view);
    }

    public void showDatePickerDialog(final Calendar calendar, final TextView dateView) {
        datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(year, monthOfYear, dayOfMonth);
                showTimePickerDialog(calendar, dateView);
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
        String preString = dateView.getText().toString();
        dateView.setText(preString + "\t\t" + String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)) + "-"
                + String.format("%02d", calendar.get(Calendar.MONTH) + 1) + "-"
                + calendar.get(Calendar.YEAR));
    }

    public void updateTimeView(Calendar calendar, TextView timeView){
        String preString = timeView.getText().toString();
        timeView.setText(preString + "\t\t" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":"
                + String.format("%02d", calendar.get(Calendar.MINUTE)));
    }

    /**
     * method called when CREATE button clicked
     */
    public void createTip(View view) {
        // fetch values
        String title = titleInput.getText().toString();
        String description = descriptionInput.getText().toString();
        final Tip tip = new Tip();
        String category = null;
        // input validation
        if(!foodCheckBox.isChecked() && !drinksCheckBox.isChecked() && !otherCheckBox.isChecked()) {
            Toast.makeText(getBaseContext(), "Please choose a category.", Toast.LENGTH_LONG).show();
        }
        else if(!validateTitle(title)) {
            Toast.makeText(getBaseContext(), "Please input a title.", Toast.LENGTH_SHORT).show();
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
