package kstr14.tipper.Activities;

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

public class CreateGroupActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String ACTIVITY_ID = "CreateGroupActivity";
    private static final int IMAGE_REQUEST = 100;

    private EditText groupNameEditText;
    private EditText groupDescriptionEditText;
    private RadioButton closedGroupRadioButton;
    private RadioButton openGroupRadioButton;

    private GoogleApiClient googleApiClient;

    private Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        googleApiClient =  new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .build();
        googleApiClient.connect();

        // initialize UI elements
        groupNameEditText = (EditText) findViewById(R.id.createGroup_ed_groupName);
        groupDescriptionEditText = (EditText) findViewById(R.id.createGroup_ed_groupDescription);
        closedGroupRadioButton = (RadioButton) findViewById(R.id.createGroup_rb_closedGroup);
        openGroupRadioButton = (RadioButton) findViewById(R.id.createGroup_rb_openGroup);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_group, menu);
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
        } else if (id == R.id.main_menu_logout){
            TipperUser user = ((Application)getApplicationContext()).getCurrentUser();
            if(user.isGoogleUser()) {
                Log.d(ACTIVITY_ID, "Google user signing out.....");
                if(googleApiClient.isConnected()) {
                    Plus.AccountApi.clearDefaultAccount(googleApiClient);
                    googleApiClient.disconnect();
                    Log.d(ACTIVITY_ID, "googleApiClient was connected, user is signed out now");
                } else {
                    Log.e(ACTIVITY_ID, "Trying to log out user, but GoogleApiClient was disconnected");
                }
            }
            try {
                ((Application)getApplicationContext()).getCurrentUser().unpin();
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
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
            startActivityForResult(chooserIntent, IMAGE_REQUEST);
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Called when the camera/gallery intent returns
     * Saved the captured/chosen image to the group, rotating it if needed and compressing
     * it to a lower quality for optimal storage
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

                    group = new Group();
                    group.setImage(file);
                    Toast.makeText(getApplicationContext(), "Image saved to group.", Toast.LENGTH_SHORT).show();

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


    public void createGroup(View view) {
        String name = groupNameEditText.getText().toString();
        String description = groupDescriptionEditText.getText().toString();
        if(group == null) {
            group = new Group();
        }


        // validate input
        if(name.length() == 0) {
            Toast.makeText(getApplicationContext(), "Please provide a name.", Toast.LENGTH_SHORT).show();
        } else if(description.length() == 0) {
            Toast.makeText(getApplicationContext(), "Please provide a description.", Toast.LENGTH_SHORT).show();
        } else {
            group.setName(name);
            group.setDescription(description);
            if(closedGroupRadioButton.isChecked()) {
                group.setClosed(true);
            } else {
                group.setClosed(false);
            }
            final TipperUser user = ((Application)getApplicationContext()).getCurrentUser();
            group.addUser(user);
            group.setCreator(user);
            group.setUuidString();
            group.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        // and now relate the group to the user as well
                        user.addGroup(group);

                        // and start intent to MyGroupsActivity
                        Intent intent = new Intent(getApplicationContext(), MyGroupsActivity.class);
                        intent.putExtra("source", ACTIVITY_ID);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error saving: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    /*** methods below required only for use of GoogleApiClient, which is necessary for logout ***/
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
