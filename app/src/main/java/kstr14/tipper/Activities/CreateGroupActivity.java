package kstr14.tipper.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.SaveCallback;

import kstr14.tipper.Application;
import kstr14.tipper.Data.Group;
import kstr14.tipper.Data.TipperUser;
import kstr14.tipper.R;

public class CreateGroupActivity extends ActionBarActivity {

    private EditText groupNameEditText;
    private EditText groupDescriptionEditText;
    private RadioButton closedGroupRadioButton;
    private RadioButton openGroupRadioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

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

    public void createGroup(View view) {
        String name = groupNameEditText.getText().toString();
        String description = groupDescriptionEditText.getText().toString();
        final Group group = new Group();

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
}
