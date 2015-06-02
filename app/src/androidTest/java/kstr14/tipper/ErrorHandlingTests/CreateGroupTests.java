package kstr14.tipper.ErrorHandlingTests;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;

import com.robotium.solo.Solo;

import kstr14.tipper.Activities.CreateGroupActivity;
import kstr14.tipper.Activities.LoginActivity;
import kstr14.tipper.Activities.MainActivity;
import kstr14.tipper.Activities.MyGroupsActivity;
import kstr14.tipper.R;

/**
 * Created by Kristine on 02-06-2015.
 */
public class CreateGroupTests extends ActivityInstrumentationTestCase2<LoginActivity> {

    private static final int TIME_OUT = 2000;
    private Solo solo;

    public CreateGroupTests() {
        super(LoginActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
        // log in
        solo.assertCurrentActivity("Wrong activity", LoginActivity.class);
        EditText usernameInput = (EditText) solo.getView(R.id.usernameDefaultLoginFragment);
        EditText passwordInput = (EditText) solo.getView(R.id.passwordDefaultLoginFragment);
        Button loginButton = (Button) solo.getView(R.id.loginButtonDefaultLoginFragment);
        solo.enterText(usernameInput, "kristine");
        solo.enterText(passwordInput, "password");
        solo.clickOnView(loginButton);

        // test we are in MainActivity and wait TIME_OUT value until tips are loaded
        solo.sleep(TIME_OUT);
        solo.assertCurrentActivity("Wrong activity", MainActivity.class);
        solo.sleep(TIME_OUT * 2);

    }

    @Override
    public void tearDown() throws Exception {
        solo.clickOnMenuItem("Log out");
        solo.sleep(TIME_OUT);
        solo.assertCurrentActivity("Wrong activity", LoginActivity.class);
        solo.finishOpenedActivities();
    }

    /**
     * Tests that when user attempts to create a group with no name,
     * an appropriate Toast message is shown
     */
    public void testNoTitle() {
        // go to MyGroupsActivity
        solo.clickOnMenuItem("My groups");
        solo.sleep(TIME_OUT);
        solo.assertCurrentActivity("Wrong activity", MyGroupsActivity.class);

        // go to CreateGroupActivity
        solo.clickOnView(solo.getCurrentActivity().findViewById(R.id.my_groups_menu_add_group));
        solo.assertCurrentActivity("Wrong activity", CreateGroupActivity.class);

        // click create and check Toast is shown and that we are still in CreateGroupActivity
        Button createButton = (Button) solo.getCurrentActivity().findViewById(R.id.createGroup_b_create);
        solo.clickOnView(createButton);
        assertTrue(solo.waitForText("Please provide a name."));
        solo.assertCurrentActivity("Wrong activity", CreateGroupActivity.class);

        // go back
        solo.goBack();
        solo.assertCurrentActivity("Wrong activity", MyGroupsActivity.class);
        solo.goBack();
    }

    /**
     * Tests that when user attempts to create a group with no description,
     * an appropriate Toast message is shown
     */
    public void testNoDescription() {
        solo.clickOnMenuItem("My groups");
        solo.sleep(TIME_OUT);
        solo.assertCurrentActivity("Wrong activity", MyGroupsActivity.class);

        // go to CreateGroupActivity
        solo.clickOnView(solo.getCurrentActivity().findViewById(R.id.my_groups_menu_add_group));
        solo.assertCurrentActivity("Wrong activity", CreateGroupActivity.class);

        // enter name, click create and check Toast is shown and that we are still in CreateGroupActivity
        EditText nameInput = (EditText) solo.getCurrentActivity().findViewById(R.id.createGroup_ed_groupName);
        solo.enterText(nameInput, "Test Group");

        Button createButton = (Button) solo.getCurrentActivity().findViewById(R.id.createGroup_b_create);
        solo.clickOnView(createButton);
        assertTrue(solo.waitForText("Please provide a description."));
        solo.assertCurrentActivity("Wrong activity", CreateGroupActivity.class);

        // go back
        solo.goBack();
        solo.assertCurrentActivity("Wrong activity", MyGroupsActivity.class);
        solo.goBack();
    }
}
