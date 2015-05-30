package kstr14.tipper.Activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import kstr14.tipper.Adapters.SpinnerGroupAdapter;
import kstr14.tipper.Application;
import kstr14.tipper.Data.Group;
import kstr14.tipper.Data.Tip;
import kstr14.tipper.Data.TipperUser;
import kstr14.tipper.ImageHelper;
import kstr14.tipper.ParseHelper;
import kstr14.tipper.R;

public class CreateTipActivity extends ActionBarActivity {

    private final static String ACTIVITY_ID = "CreateTipActivity";

    private static final int CAPTURE_IMAGE_REQUEST = 100;

    private EditText titleInput;
    private EditText descriptionInput;
    private RadioButton foodRadioButton;
    private RadioButton drinksRadioButton;
    private RadioButton otherRadioButton;
    private SeekBar priceSeekBar;
    private TextView priceView;
    private LinearLayout startDateLayout;
    private LinearLayout endDateLayout;
    private TextView startDateView;
    private TextView startTimeView;
    private TextView endDateView;
    private TextView endTimeView;
    private DatePickerDialog datePicker;
    private TimePickerDialog timePicker;
    private Spinner groupChoice;
    private Spinner repeatStyle;

    private Calendar current;
    private Calendar chosenStartDate;
    private Calendar chosenEndDate;
    private int chosenPrice;

    private String sourceActivity;
    private Tip tip;
    private Uri outputFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_tip);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // set current date to correct timezone
        TimeZone timeZone = TimeZone.getTimeZone("Australia/Melbourne");
        current = Calendar.getInstance(timeZone);
        chosenStartDate = Calendar.getInstance();
        chosenEndDate = Calendar.getInstance();

        // initialize all UI elements
        titleInput = (EditText) findViewById(R.id.createTip_ed_title);
        descriptionInput = (EditText) findViewById(R.id.createTip_ed_description);
        foodRadioButton = (RadioButton) findViewById(R.id.createTip_rb_food);
        drinksRadioButton = (RadioButton) findViewById(R.id.createTip_rb_drinks);
        otherRadioButton = (RadioButton) findViewById(R.id.createTip_rb_other);
        priceSeekBar = (SeekBar) findViewById(R.id.createTip_sb_price);
        priceView = (TextView) findViewById(R.id.createTip_tv_price);
        startDateLayout = (LinearLayout) findViewById(R.id.createTip_ll_startDate);
        endDateLayout = (LinearLayout) findViewById(R.id.createTip_ll_endDate);
        startDateView = (TextView) findViewById(R.id.createTip_tv_startDate);
        startTimeView = (TextView) findViewById(R.id.createTip_tv_startTime);
        endDateView = (TextView) findViewById(R.id.createTip_tv_endDate);
        endTimeView = (TextView) findViewById(R.id.createTip_tv_endTime);
        groupChoice = (Spinner) findViewById(R.id.createTip_s_groupChoice);
        repeatStyle = (Spinner) findViewById(R.id.createTip_s_repeatStyle);

        // prevent EditText boxes to resize themselves when user enters a lot of text
        titleInput.setMaxLines(1);
        descriptionInput.setMaxLines(1);

        // set today's date as default date on start and end date TextViews
        updateDateView(current, startDateView);
        updateDateView(current, endDateView);
        updateTimeView(current, startTimeView);
        updateTimeView(current, endTimeView);

        // populate the repeatStyle spinner with choices
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this,
                R.array.repeatstyles_array, R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        repeatStyle.setAdapter(arrayAdapter);

        // if sourceActivity was ShowGroupActivity, the only group in the list should be that group
        sourceActivity = getIntent().getExtras().getString("source");
        if(sourceActivity != null && sourceActivity.equals("ShowGroupActivity")) {
            String groupID = getIntent().getExtras().getString("groupID");
            if(groupID != null) {
                ParseQuery<Group> query = ParseQuery.getQuery("Group");
                query.whereEqualTo("uuid", groupID);
                query.findInBackground(new FindCallback<Group>() {
                    @Override
                    public void done(List<Group> list, ParseException e) {
                        if(e == null) {
                            if(!list.isEmpty()) {
                                SpinnerGroupAdapter adapter = new SpinnerGroupAdapter(
                                        getApplicationContext(), R.layout.simple_spinner_item, list);
                                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                                groupChoice.setAdapter(adapter);
                            } else {
                                Log.d(ACTIVITY_ID, "Intent sourceActivity is ShowGroupActivity but could not fetch group by groupID");
                            }
                        } else {
                            Log.d(ACTIVITY_ID, "Parse error: \n" + e.getMessage());
                        }
                    }
                });
            } else {
                Log.d(ACTIVITY_ID, "Intent sourceActivity is ShowGroupActivity but groupID is null");
            }
        } else if(sourceActivity != null && sourceActivity.equals("MainActivity")){

            // otherwise the spinner will contain all the groups the current user is member of
            TipperUser user = ((Application) getApplicationContext()).getCurrentUser();
            SpinnerGroupAdapter adapter = null;
            try {
                List<Group> groups = ParseHelper.getUsersGroups(user);
                // add a dummy group as well, for being able to create a tip that has no group
                ParseQuery<Group> dummyQuery = ParseQuery.getQuery("Group");
                Group dummyGroup = dummyQuery.whereEqualTo("uuid", "dummy").getFirst();
                if(dummyGroup != null) groups.add(dummyGroup);
                adapter = new SpinnerGroupAdapter(this, R.layout.simple_spinner_item, groups);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
            groupChoice.setAdapter(adapter);
        } else {
            Log.d(ACTIVITY_ID, "No sourceActivity provided!");
        }

        // handle price SeekBar changes
        priceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

    /**
     * Presents a DatePickerDialog to the user, which in turn presents a TimePickerDialog
     * @param calendar the Calendar object to hold the chosen values
     * @param dateView the TextView where the chosen date is set
     * @param timeView the TextView where the chosen time is set
     */
    public void showDateTimePicker(final Calendar calendar, final TextView dateView, final TextView timeView) {
        showDatePickerDialog(calendar, dateView, timeView);
    }

    /**
     * Presents a DatePickerDialog and sets the chosen values to the given TextView
     * @param calendar the Calendar object to hold the chosen values
     * @param dateView the TextView where the chosen date is set
     * @param timeView the TextView where the chosen time is set
     */
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

    /**
     * Presents a TimePickerDialog and sets the chosen values to the given TextView
     * @param calendar the Calendar object to hold the chosen values
     * @param timeView the TextView where the chosen time is set
     */
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

    /**
     * Sets the given TextView to a nice String representation of the date in the given Calendar
     * @param calendar the Calendar holding the date to be set
     * @param dateView the TextView to be set
     */
    public void updateDateView(Calendar calendar, TextView dateView) {
        dateView.setText(String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)) + "-"
                + String.format("%02d", calendar.get(Calendar.MONTH) + 1) + "-"
                + calendar.get(Calendar.YEAR));
    }

    /**
     * Sets the given TextView to a nice String representation of the time in the given Calendar
     * @param calendar the Calendar holding the time to be set
     * @param timeView the TextView to be set
     */
    public void updateTimeView(Calendar calendar, TextView timeView) {
        timeView.setText(String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":"
                + String.format("%02d", calendar.get(Calendar.MINUTE)));
    }

    /**
     * method called when CREATE button clicked
     */
    public void createTip(View view) {
        if(tip == null) {
            tip = new Tip();
        }
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

        // set group and set tip to private is chosen group is closed
        final Group group = (Group) groupChoice.getSelectedItem();
        if(group != null) {
            if(!group.getUuidString().equals("dummy")) {
                if(group.isClosed()) {
                    tip.setPrivate(true);
                } else {
                    tip.setPrivate(false);
                }
                tip.setGroup(group);
            } else {
                tip.setPrivate(false);
            }
        }

        // input validation
        if(!foodRadioButton.isChecked() && !drinksRadioButton.isChecked() && !otherRadioButton.isChecked()) {
            Toast.makeText(getBaseContext(), "Please choose a category.", Toast.LENGTH_LONG).show();
        } else if(!validateTitle(title)) {
            Toast.makeText(getBaseContext(), "Please input a title.", Toast.LENGTH_SHORT).show();
        } else if (chosenStartDate.before(current)) {
            Toast.makeText(getBaseContext(), "Please input a valid start date.", Toast.LENGTH_SHORT).show();
        } else if (chosenEndDate.before(chosenStartDate)) {
            Toast.makeText(getBaseContext(), "End date cannot be before start date.", Toast.LENGTH_SHORT).show();
        } else {
            if (foodRadioButton.isChecked()) tip.setCategory("Food");
            else if (drinksRadioButton.isChecked()) tip.setCategory("Drinks");
            else if (otherRadioButton.isChecked()) tip.setCategory("Other");

            tip.setTitle(title);
            tip.setDescription(description);
            tip.setDownvotes(0);
            tip.setUpvotes(0);
            tip.setPrice(chosenPrice);
            tip.setEndDate(chosenEndDate.getTime());
            tip.setStartDate(chosenStartDate.getTime());
            tip.setUuidString();
            tip.setCreator(((Application) getApplicationContext()).getCurrentUser());
            tip.saveInBackground(new SaveCallback() {

                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        if(group != null) {
                            // now make the relation from group to tip
                            group.addTip(tip);
                            group.saveInBackground();
                        }
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("source", ACTIVITY_ID);
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
    public Intent getSupportParentActivityIntent() {
        return getParentActivity();
    }

    @Override
    public Intent getParentActivityIntent() {
        return getParentActivity();
    }

    private Intent getParentActivity() {
        Intent intent = null;
        if (sourceActivity.equals("MainActivity")) {
            intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        } else if (sourceActivity.equals("ShowGroupActivity")){
            intent = new Intent(this, ShowGroupActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else {
            Log.d(ACTIVITY_ID, "No sourceActivity provided.");
        }
        return intent;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_tip, menu);
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
        } else if (id == R.id.camera) {
            // bundle camera intent and gallery intent together to a chooser intent
            List<Intent> intents = new ArrayList<>();

            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            intents.add(cameraIntent);

            Intent galleryIntent = new Intent();
            galleryIntent.setType("image/*");
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[intents.size()]));
            startActivityForResult(chooserIntent, CAPTURE_IMAGE_REQUEST);
        } else if (id == R.id.location) {
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Called when the camera/gallery intent returns
     * Saved the captured/chosen image to the tip, rotating it if needed and compressing
     * it to a lower quality for optimal storage
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data == null ? null : data.getData();
                try {
                    Bitmap bitmap = ImageHelper.decodeBitmapFromUri(getApplicationContext(), selectedImageUri, ImageHelper.IMAGE_SIZE);

                    // check orientation - if portrait the image will have rotated so we need to rotate it back
                    ExifInterface ei = new ExifInterface(ImageHelper.getRealPathFromURI(getApplicationContext(), selectedImageUri));
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                    switch(orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            bitmap = ImageHelper.rotateBitmap(bitmap, 90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            bitmap = ImageHelper.rotateBitmap(bitmap, 180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            bitmap = ImageHelper.rotateBitmap(bitmap, 270);
                            break;
                    }

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, ImageHelper.COMPRESSION_QUALITY, stream);
                    byte[] image = stream.toByteArray();

                    // Create the ParseFile and save it to the tip
                    ParseFile file = new ParseFile("image.jpeg", image);
                    file.saveInBackground();

                    tip = new Tip();
                    tip.setImage(file);
                    Toast.makeText(getApplicationContext(), "Image saved to tip.", Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == RESULT_CANCELED) {
                Log.d(ACTIVITY_ID, "User cancelled image capture.");
            } else {
                Toast.makeText(getApplicationContext(), "Image capture failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }






}
