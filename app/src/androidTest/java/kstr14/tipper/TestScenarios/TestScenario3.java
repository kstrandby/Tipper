package kstr14.tipper.TestScenarios;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;

import com.robotium.solo.Solo;

import kstr14.tipper.Activities.LoginActivity;
import kstr14.tipper.Activities.MainActivity;
import kstr14.tipper.Activities.MyGroupsActivity;
import kstr14.tipper.Activities.ShowGroupActivity;
import kstr14.tipper.R;

/**
 * Created by Kristine on 03-06-2015.
 */
public class TestScenario3 extends ActivityInstrumentationTestCase2<LoginActivity> {

    private static final int TIME_OUT = 2000;
    private Solo solo;

    public TestScenario3() {
        super(LoginActivity.class);
    }


    @Override
    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    /**
     * Tests the following scenario:
     *  - User logs in to the app
     *  - User goes to MyGroupsActivity and clicks on one of the groups
     *  - User leaves group
     *  - User joins same group again
     *  - User logs out
     */
    public void testScenario3() {
        // log in
        solo.assertCurrentActivity("Wrong activity", LoginActivity.class);
        EditText usernameInput = (EditText) solo.getView(R.id.usernameDefaultLoginFragment);
        EditText passwordInput = (EditText) solo.getView(R.id.passwordDefaultLoginFragment);
        Button loginButton = (Button) solo.getView(R.id.loginButtonDefaultLoginFragment);
        solo.enterText(usernameInput, "TestUser1");
        solo.enterText(passwordInput, "password");
        solo.clickOnView(loginButton);
        solo.sleep(TIME_OUT);

        // test we are in MainActivity and wait TIME_OUT value until tips are loaded
        solo.assertCurrentActivity("Wrong activity", MainActivity.class);
        solo.sleep(TIME_OUT * 2);

        // go to MyGroupsActivity and click on a group
        solo.clickOnMenuItem("My groups");
        solo.sleep(TIME_OUT);
        solo.assertCurrentActivity("Wrong activity", MyGroupsActivity.class);
        solo.sleep(TIME_OUT*2);
        solo.clickInList(1);
        solo.sleep(TIME_OUT);
        solo.assertCurrentActivity("Wrong activity", ShowGroupActivity.class);
        solo.sleep(TIME_OUT);

        // click leave group, check that leave group button is no longer visible
        Button leaveGroup = (Button) solo.getView(R.id.showGroups_b_leaveGroup);
        assertNotNull(leaveGroup);
        solo.clickOnView(leaveGroup);
        solo.sleep(TIME_OUT);
        assertFalse(solo.waitForText("LEAVE GROUP"));

        // click join group and check that leave group is visible again
        Button joinGroup = (Button) solo.getView(R.id.showGroups_b_joinGroup);
        assertNotNull(joinGroup);
        solo.clickOnView(joinGroup);
        solo.sleep(TIME_OUT);
        assertTrue(solo.waitForText("LEAVE GROUP"));
        solo.sleep(TIME_OUT);

        // log out
        solo.clickOnMenuItem("Log out");
        solo.sleep(TIME_OUT);
        solo.assertCurrentActivity("Wrong activity", LoginActivity.class);

    }

}
