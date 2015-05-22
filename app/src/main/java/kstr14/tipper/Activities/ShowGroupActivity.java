package kstr14.tipper.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.List;

import kstr14.tipper.Adapters.TipBaseAdapter;
import kstr14.tipper.Application;
import kstr14.tipper.Data.Group;
import kstr14.tipper.Data.Tip;
import kstr14.tipper.Data.TipperUser;
import kstr14.tipper.R;

public class ShowGroupActivity extends ActionBarActivity {

    private Menu menu;

    private ImageView imageView;
    private ListView listView;
    private TextView descriptionView;
    private Group group;
    private TipperUser currentUser;

    private TipBaseAdapter adapter;

    private boolean member;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_group);

        // check if the group we are showing is a group the current user is not member of
        // or a group the user is member of, to show the correct fragment
        currentUser = ((Application)getApplicationContext()).getCurrentUser();
        String groupID = getIntent().getExtras().getString("ID");
        ParseQuery<Group> query = currentUser.getGroups().getQuery();
        query.whereEqualTo("uuid", groupID);
        query.findInBackground(new FindCallback<Group>() {
            @Override
            public void done(List<Group> list, ParseException e) {
                if (list.isEmpty()) {
                    // user not member of group - show fragment with Join button
                    GroupNotMemberFragment notMemberFragment = new GroupNotMemberFragment();
                    getFragmentManager().beginTransaction()
                            .add(R.id.ShowGroupActivity_fragment_container, notMemberFragment).commit();
                    member = false;
                    MenuItem addItem = menu.findItem(R.id.action_add_tip_to_group);
                    addItem.setVisible(false);
                } else {
                    // user member of group - show fragment with Leave button
                    GroupMemberFragment memberFragment = new GroupMemberFragment();
                    getFragmentManager().beginTransaction()
                            .add(R.id.ShowGroupActivity_fragment_container, memberFragment).commit();
                    group = list.get(0);
                    member = true;
                }
            }
        });


        // initialize UI elements
        imageView = (ImageView) findViewById(R.id.showGroup_iv_groupImage);
        listView = (ListView) findViewById(R.id.showGroup_lv_groups);
        descriptionView = (TextView) findViewById(R.id.showGroup_tv_description);

        if(group == null) {

            // fetch group object from database
            ParseQuery<Group> groupQuery = ParseQuery.getQuery("Group");
            groupQuery.whereEqualTo("uuid", groupID);
            groupQuery.getFirstInBackground(new GetCallback<Group>() {
                @Override
                public void done(Group object, ParseException e) {
                    if (e == null) {
                        group = object;

                        // set actionBar title to name of group
                        getSupportActionBar().setTitle(group.getName());
                        descriptionView.setText(group.getDescription());

                        // fetch tips of group
                        group.getTips().getQuery().findInBackground(new FindCallback<Tip>() {
                            @Override
                            public void done(List<Tip> list, ParseException e) {
                                adapter = new TipBaseAdapter(getApplicationContext(), list);
                                listView.setAdapter(adapter);
                            }
                        });

                        // hide the tips, if the current user is not a member and the group is closed
                        if(!member && group.isClosed()) {
                            listView.setVisibility(View.INVISIBLE);
                        }

                        //TODO set image

                    } else {
                        e.printStackTrace();
                    }
                }
            });
        } else {

            // set actionBar title to name of group
            getSupportActionBar().setTitle(group.getName());
                descriptionView.setText(group.getDescription());

            // fetch tips of group and set up adapter and listview
            group.getTips().getQuery().findInBackground(new FindCallback<Tip>() {
                @Override
                public void done(List<Tip> list, ParseException e) {
                    adapter = new TipBaseAdapter(getApplicationContext(), list);
                    listView.setAdapter(adapter);
                }
            });

        }
    }
    public void joinGroup(View view) {
        group.addUser(currentUser);
        group.saveInBackground();
        currentUser.addGroup(group);
        currentUser.saveInBackground();
        // refresh the screen with the MemberFragment
        GroupMemberFragment memberFragment = new GroupMemberFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.ShowGroupActivity_fragment_container, memberFragment).commit();
        member = true;
        // set add button on action bar visible and list of tips
        MenuItem addItem = menu.findItem(R.id.action_add_tip_to_group);
        addItem.setVisible(true);
        listView.setVisibility(View.VISIBLE);
    }

    public void leaveGroup(View view) {
        group.removeUser(currentUser);
        group.saveInBackground();
        currentUser.removeGroup(group);
        currentUser.saveInBackground();
        // refresh the screen with the notMemberFragment
        GroupNotMemberFragment notMemberFragment = new GroupNotMemberFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.ShowGroupActivity_fragment_container, notMemberFragment).commit();
        member = false;
        // check if the group is closed, so we need to hide the tips now the user is not member
        if(group.isClosed()) {
            listView.setVisibility(View.INVISIBLE);
        }
        MenuItem addItem = menu.findItem(R.id.action_add_tip_to_group);
        addItem.setVisible(false);
    }

    public void updateTipList() {
        // fetch tips of group
        group.getTips().getQuery().findInBackground(new FindCallback<Tip>() {
            @Override
            public void done(List<Tip> list, ParseException e) {
                adapter = new TipBaseAdapter(getApplicationContext(), list);
                listView.setAdapter(adapter);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_show_group, menu);
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
        } else if (id == R.id.action_add_tip_to_group) {
            Intent intent = new Intent(ShowGroupActivity.this, CreateTipActivity.class);
            intent.putExtra("source", "ShowGroupActivity");
            intent.putExtra("groupID", group.getUuidString());
            startActivityForResult(intent, MainActivity.CREATE_TIP_REQUEST);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(requestCode == MainActivity.CREATE_TIP_REQUEST) {
            if(resultCode == RESULT_OK) {
                updateTipList();
            }
        }
    }
}
