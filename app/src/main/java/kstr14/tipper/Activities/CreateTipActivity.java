package kstr14.tipper.Activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
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

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.plus.Plus;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import kstr14.tipper.Adapters.SpinnerGroupAdapter;
import kstr14.tipper.Application;
import kstr14.tipper.Data.Group;
import kstr14.tipper.Data.Tip;
import kstr14.tipper.Data.TipperUser;
import kstr14.tipper.ErrorHandler;
import kstr14.tipper.ImageHelper;
import kstr14.tipper.MapsHelper;
import kstr14.tipper.R;

public class CreateTipActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public final static String ACTIVITY_ID = "CreateTipActivity";

    private static final int NUMBER_OF_RECURRENCES = 5;

    private static final int RECURRENCE_ONCE = 1;
    private static final int RECURRENCE_DAILY = 2;
    private static final int RECURRENCE_WEEKLY = 3;
    private static final int RECURRENCE_MONTHLY = 4;
    private static final int RECURRENCE_YEARLY = 5;

    private static final int CAPTURE_IMAGE_REQUEST = 100;
    private static final int CHOOSE_LOCATION_REQUEST = 200;

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

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_tip);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .build();
        googleApiClient.connect();

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

        // if sourceActivity was ShowGroupActivity, the only group in the the group spinner should be that group
        sourceActivity = getIntent().getExtras().getString("source");
        if (sourceActivity != null && sourceActivity.equals("ShowGroupActivity")) {
            String groupID = getIntent().getExtras().getString("groupID");
            setUpGroupSpinnerOneGroup(groupID);
        } else if (sourceActivity != null && sourceActivity.equals("MainActivity")) {
            // otherwise the spinner will contain all the groups the current user is member of
            setUpGroupSpinnerAllGroups();

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

    /**
     * Sets the group spinner to all groups the user is a member of
     */
    public void setUpGroupSpinnerAllGroups() {
        TipperUser user = ((Application) getApplicationContext()).getCurrentUser();
        if (user != null) {
            user.getGroups().getQuery().findInBackground(new FindCallback<Group>() {
                @Override
                public void done(final List<Group> list, ParseException e) {
                    if (e == null && list != null) {
                        // add a dummy group as well, for being able to create a tip that has no group
                        ParseQuery<Group> dummyQuery = ParseQuery.getQuery("Group");
                        dummyQuery.whereEqualTo("uuid", "dummy").getFirstInBackground(new GetCallback<Group>() {
                            @Override
                            public void done(Group group, ParseException e) {
                                if (e == null) {
                                    if (group != null) list.add(group);
                                    SpinnerGroupAdapter adapter = new SpinnerGroupAdapter(getApplicationContext(), R.layout.simple_spinner_item, list);
                                    adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                                    groupChoice.setAdapter(adapter);
                                } else {
                                    Log.e(ACTIVITY_ID, "Parse error: " + e.getStackTrace().toString());
                                    ErrorHandler.showConnectionErrorAlert(CreateTipActivity.this, getParentActivity());
                                }

                            }
                        });
                    } else {
                        Log.e(ACTIVITY_ID, "Parse error: " + e.getMessage());
                        e.printStackTrace();
                        ErrorHandler.showConnectionErrorAlert(CreateTipActivity.this, getParentActivity());
                    }

                }
            });
        } else {
            Log.d(ACTIVITY_ID, "User object is null");
        }
    }

    /**
     * Sets up the group spinner with only one group, namely the group
     * specified by its group uuid
     *
     * @param groupUuid, the uuid specifying the group
     */
    public void setUpGroupSpinnerOneGroup(String groupUuid) {
        if (groupUuid != null) {
            ParseQuery<Group> query = ParseQuery.getQuery("Group");
            query.whereEqualTo("uuid", groupUuid);
            query.findInBackground(new FindCallback<Group>() {
                @Override
                public void done(List<Group> list, ParseException e) {
                    if (e == null && list != null) {
                        if (!list.isEmpty()) {
                            SpinnerGroupAdapter adapter = new SpinnerGroupAdapter(
                                    getApplicationContext(), R.layout.simple_spinner_item, list);
                            adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                            groupChoice.setAdapter(adapter);
                        } else {
                            Log.e(ACTIVITY_ID, "Intent sourceActivity is ShowGroupActivity but could not fetch group by groupID");
                        }
                    } else {
                        if (e != null) {
                            Log.e(ACTIVITY_ID, "Parse error: \n" + e.getMessage());
                            e.printStackTrace();
                            ErrorHandler.showConnectionErrorAlert(CreateTipActivity.this, getParentActivity());

                        }
                    }
                }
            });
        } else {
            Log.e(ACTIVITY_ID, "Intent sourceActivity is ShowGroupActivity but groupID is null");
        }
    }


    /**
     * Presents a DatePickerDialog to the user, which in turn presents a TimePickerDialog
     *
     * @param calendar the Calendar object to hold the chosen values
     * @param dateView the TextView where the chosen date is set
     * @param timeView the TextView where the chosen time is set
     */
    public void showDateTimePicker(final Calendar calendar, final TextView dateView, final TextView timeView) {
        showDatePickerDialog(calendar, dateView, timeView);
    }

    /**
     * Presents a DatePickerDialog and sets the chosen values to the given TextView
     *
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
     *
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
     *
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
     *
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
    public void createTipClicked(View view) {
        if (tip == null) {
            tip = new Tip();
        }
        int recurrence = 0;
        // fetch values
        String title = titleInput.getText().toString();
        String description = descriptionInput.getText().toString();

        String selectedRepeatStyle = repeatStyle.getSelectedItem().toString();

        if (selectedRepeatStyle.equals("One time event")) {
            recurrence = RECURRENCE_ONCE;
        } else if (selectedRepeatStyle.equals("Daily")) {
            recurrence = RECURRENCE_DAILY;
        } else if (selectedRepeatStyle.equals("Weekly")) {
            recurrence = RECURRENCE_WEEKLY;
        } else if (selectedRepeatStyle.equals("Monthly")) {
            recurrence = RECURRENCE_MONTHLY;
        } else if (selectedRepeatStyle.equals("Yearly")) {
            recurrence = RECURRENCE_YEARLY;
        }

        // set group and set tip to private is chosen group is closed
        final Group group = (Group) groupChoice.getSelectedItem();
        if (group != null) {
            if (!group.getUuidString().equals("dummy")) {
                if (group.isClosed()) {
                    tip.setPrivate(true);
                } else {
                    tip.setPrivate(false);
                }
            } else {
                tip.setPrivate(false);
            }
            tip.setGroup(group);

        } else {
            Log.e(ACTIVITY_ID, "Group is null");
        }

        // input validation
        if (!foodRadioButton.isChecked() && !drinksRadioButton.isChecked() && !otherRadioButton.isChecked()) {
            Toast.makeText(getBaseContext(), "Please choose a category.", Toast.LENGTH_LONG).show();
        } else if (!validateTitle(title)) {
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
            TipperUser user = ((Application) getApplicationContext()).getCurrentUser();
            if(user == null) {
                Log.e(ACTIVITY_ID, "User object is null");
            } else {
                tip.setCreator(user);
            }

            createTip(tip, group, recurrence);

        }
    }

    private void createTip(final Tip tip, final Group group, int recurrence) {
        if (recurrence == RECURRENCE_ONCE) {
            tip.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        if (group != null) {
                            // now make the relation from group to tip
                            group.addTip(tip);
                            group.saveInBackground();
                        }
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("source", ACTIVITY_ID);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Error saving. Tip might not be saved.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else if (recurrence == RECURRENCE_DAILY) {
            Date startDate = tip.getStartDate();
            Date endDate = tip.getEndDate();
            final List<Tip> tips = new ArrayList<>();
            for (int i = 0; i < NUMBER_OF_RECURRENCES; i++) {
                final Tip newTip = tip.copy();
                startDate = addDaysToDate(startDate, 1);
                endDate = addDaysToDate(endDate, 1);
                newTip.setStartDate(startDate);
                newTip.setEndDate(endDate);
                tips.add(newTip);

            }
            ParseObject.saveAllInBackground(tips, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Toast.makeText(getApplicationContext(), "Error saving. Tip might not be saved.", Toast.LENGTH_SHORT).show();
                    } else {
                        for (Tip t : tips) {
                            if (group != null) {
                                // now make the relation from group to tip
                                group.addTip(t);
                                group.saveInBackground();
                            }
                        }
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("source", ACTIVITY_ID);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            });
        } else if (recurrence == RECURRENCE_WEEKLY) {
            Date startDate = tip.getStartDate();
            Date endDate = tip.getEndDate();
            final List<Tip> tips = new ArrayList<>();
            for (int i = 0; i < NUMBER_OF_RECURRENCES; i++) {
                final Tip newTip = tip.copy();
                startDate = addDaysToDate(startDate, 7);
                endDate = addDaysToDate(endDate, 7);
                newTip.setStartDate(startDate);
                newTip.setEndDate(endDate);
                tips.add(newTip);
            }
            ParseObject.saveAllInBackground(tips, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Toast.makeText(getApplicationContext(), "Error saving. Tip might not be saved.", Toast.LENGTH_SHORT).show();
                    } else {
                        for (Tip t : tips) {
                            if (group != null) {
                                // now make the relation from group to tip
                                group.addTip(t);
                                group.saveInBackground();
                            }
                        }
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("source", ACTIVITY_ID);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            });
        } else if (recurrence == RECURRENCE_MONTHLY) {
            Date startDate = tip.getStartDate();
            Date endDate = tip.getEndDate();
            final List<Tip> tips = new ArrayList<>();
            for (int i = 0; i < NUMBER_OF_RECURRENCES; i++) {
                final Tip newTip = tip.copy();
                startDate = addMonthsToDate(startDate, 1);
                endDate = addMonthsToDate(endDate, 1);
                newTip.setStartDate(startDate);
                newTip.setEndDate(endDate);
                tips.add(newTip);
            }
            ParseObject.saveAllInBackground(tips, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Toast.makeText(getApplicationContext(), "Error saving. Tip might not be saved.", Toast.LENGTH_SHORT).show();
                    } else {
                        for (Tip t : tips) {
                            if (group != null) {
                                // now make the relation from group to tip
                                group.addTip(t);
                                group.saveInBackground();
                            }
                        }
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("source", ACTIVITY_ID);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            });
        } else if (recurrence == RECURRENCE_YEARLY) {
            Date startDate = tip.getStartDate();
            Date endDate = tip.getEndDate();
            final List<Tip> tips = new ArrayList<>();
            for (int i = 0; i < NUMBER_OF_RECURRENCES; i++) {
                final Tip newTip = tip.copy();
                startDate = addDaysToDate(startDate, 365);
                endDate = addDaysToDate(endDate, 365);
                newTip.setStartDate(startDate);
                newTip.setEndDate(endDate);
                tips.add(newTip);
            }
            ParseObject.saveAllInBackground(tips, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Toast.makeText(getApplicationContext(), "Error saving. Tip might not be saved.", Toast.LENGTH_SHORT).show();
                    } else {
                        for (Tip t : tips) {
                            if (group != null) {
                                // now make the relation from group to tip
                                group.addTip(t);
                                group.saveInBackground();
                            }
                        }
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("source", ACTIVITY_ID);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            });
        }
    }

    private Date addMonthsToDate(Date date, int months) {
        Date newDate = new Date(date.getTime());

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(newDate);
        calendar.add(Calendar.MONTH, months);
        newDate.setTime(calendar.getTime().getTime());

        return newDate;
    }

    private Date addDaysToDate(Date date, int days) {
        Date newDate = new Date(date.getTime());

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(newDate);
        calendar.add(Calendar.DATE, days);
        newDate.setTime(calendar.getTime().getTime());

        return newDate;
    }


    public boolean validateTitle(String title) {
        if (title.length() != 0) return true;
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
        AlertDialog.Builder alert = new AlertDialog.Builder(CreateTipActivity.this);
        alert.setTitle("Cancel creation of tip?");
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
        return null;
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
            } else if (user.isFacebookUser()) {
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
            startActivityForResult(intent, CHOOSE_LOCATION_REQUEST);
        } else if (id == R.id.about) {
            Intent intent = new Intent(this, AboutActivity.class);
            intent.putExtra("source", ACTIVITY_ID);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Called when the camera/gallery intent returns
     * Saved the captured/chosen image to the tip, rotating it if needed and compressing
     * it to a lower quality for optimal storage
     *
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

                    switch (orientation) {
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

                    if (tip == null) {
                        tip = new Tip();
                    }
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
        } else if (requestCode == CHOOSE_LOCATION_REQUEST) {
            if (resultCode == RESULT_OK) {
                LatLng latLng = data.getParcelableExtra("latLng");
                ParseGeoPoint location = MapsHelper.getParseGeoPointFromLatLng(latLng);
                if (tip == null) {
                    tip = new Tip();
                }
                tip.setLocation(location);

            } else if (resultCode == RESULT_CANCELED) {
                Log.d(ACTIVITY_ID, "User cancelled location picking.");
            } else {
                Toast.makeText(getApplicationContext(), "Location picking failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alert = new AlertDialog.Builder(CreateTipActivity.this);
        alert.setTitle("Cancel creation of tip?");
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    /**
     * methods below required only for use of GoogleApiClient, which is necessary for logout **
     */
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
