package kstr14.tipper.Activities;

import android.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kstr14.tipper.Application;
import kstr14.tipper.Data.Group;
import kstr14.tipper.Data.TipperUser;
import kstr14.tipper.ImageHelper;
import kstr14.tipper.R;

/**
 * Activity displaying screen for creating a group
 * The creation of a group requires the following attributes:
 * - A title of the group
 * - A description of the group
 * - Whether the group is closed or open
 * In addition, it is optional to include an image (which can be captured or chosen in gallery
 * by click on the camera icon on ActionBar) and to include location (which can be added using Google maps by
 * clicking the location icon on ActionBar)
 */
public class CreateGroupActivity extends ActionBarActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // ACTIVITY_ID is used for logging and keeping track of navigation between activities
    public static final String ACTIVITY_ID = "CreateGroupActivity";

    private static final int IMAGE_REQUEST = 100;

    // UI elements
    private EditText groupNameEditText;
    private EditText groupDescriptionEditText;
    private RadioButton closedGroupRadioButton;

    private GoogleApiClient googleApiClient;
    private Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .build();
        googleApiClient.connect();

        // initialize UI elements
        groupNameEditText = (EditText) findViewById(R.id.createGroup_ed_groupName);
        groupDescriptionEditText = (EditText) findViewById(R.id.createGroup_ed_groupDescription);
        closedGroupRadioButton = (RadioButton) findViewById(R.id.createGroup_rb_closedGroup);
    }

    /**
     * Creates the ActionBar menu
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_group, menu);
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
                    Log.d(ACTIVITY_ID, "Logged out Google user.");
                } else {
                    Log.e(ACTIVITY_ID, "Trying to log out user, but GoogleApiClient was disconnected.");
                }
            } else if (user.isFacebookUser()) {
                Log.d(ACTIVITY_ID, "Facebook user signing out......");
                FacebookSdk.sdkInitialize(getApplicationContext());
                LoginManager.getInstance().logOut();
            }
            try {
                ((Application) getApplicationContext()).getCurrentUser().unpin();
            } catch (ParseException e) {
                Log.e(ACTIVITY_ID, "Parse error: " + e.getMessage());
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
            startActivityForResult(chooserIntent, IMAGE_REQUEST);
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
     * Saves the captured/chosen image to the group, rotating it if needed and compressing
     * it to a lower quality for optimal storage
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data == null ? null : data.getData();
                try {
                    Bitmap bitmap = ImageHelper.decodeBitmapFromUri(getApplicationContext(), selectedImageUri, ImageHelper.IMAGE_SIZE);

                    // check orientation - if portrait, the image will have rotated so we need to rotate it back
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

                    // Create the ParseFile and save it to the group
                    ParseFile file = new ParseFile("image.jpeg", image);
                    file.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Toast.makeText(getApplicationContext(), "Error saving image to group.", Toast.LENGTH_SHORT).show();
                                Log.e(ACTIVITY_ID, "Parse error: " + e.getMessage());
                                e.printStackTrace();
                            } else {
                                Toast.makeText(getApplicationContext(), "Image saved to group.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    if (group == null) {
                        group = new Group();
                    }
                    group.setImage(file);

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

    /**
     * Called when user clicks CREATE button
     * Creates the group with the given attributes and saves it to the database
     *
     * @param view
     */
    public void createGroup(View view) {
        String name = groupNameEditText.getText().toString();
        String description = groupDescriptionEditText.getText().toString();
        if (group == null) {
            group = new Group();
        }

        // input validation
        if (name.length() == 0) {
            Toast.makeText(getApplicationContext(), "Please provide a name.", Toast.LENGTH_SHORT).show();
        } else if (description.length() == 0) {
            Toast.makeText(getApplicationContext(), "Please provide a description.", Toast.LENGTH_SHORT).show();
        } else {
            // create the group with the given attributes
            group.setName(name);
            group.setDescription(description);
            if (closedGroupRadioButton.isChecked()) {
                group.setClosed(true);
            } else {
                group.setClosed(false);
            }
            final TipperUser user = ((Application) getApplicationContext()).getCurrentUser();
            if (user != null) {
                group.addUser(user);
                group.setCreator(user);
                group.setUuidString();
                group.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            // and now relate the group to the user as well before going to MyGroupsActivity
                            user.addGroup(group);
                            Intent intent = new Intent(getApplicationContext(), MyGroupsActivity.class);
                            intent.putExtra("source", ACTIVITY_ID);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Error saving the group, please check the internet connection and try again.", Toast.LENGTH_SHORT).show();
                            Log.e(ACTIVITY_ID, "Parse error: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                Log.e(ACTIVITY_ID, "User object is null");
            }
        }
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
     * Shows AlertDialog asking the user to confirm group creation before going back
     *
     * @return
     */
    private Intent getParentActivity() {
        AlertDialog.Builder alert = new AlertDialog.Builder(CreateGroupActivity.this);
        alert.setTitle("Cancel creation of group?");
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

    /**
     * Handles hardware back button clicks
     * Shows AlertDialog asking the user to confirm group creation before going back
     */
    @Override
    public void onBackPressed() {
        AlertDialog.Builder alert = new AlertDialog.Builder(CreateGroupActivity.this);
        alert.setTitle("Cancel creation of group?");
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
