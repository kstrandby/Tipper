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
public class TestScenario4 extends ActivityInstrumentationTestCase2<LoginActivity> {

    private static final int TIME_OUT = 2000;
    private Solo solo;

    public TestScenario4() {
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

    public void testScenario4() {
        // log in
        solo.assertCurrentActivity("Wrong activity", LoginActivity.class);
        EditText usernameInput = (EditText) solo.getView(R.id.usernameDefaultLoginFragment);
        EditText passwordInput = (EditText) solo.getView(R.id.passwordDefaultLoginFragment);
        Button loginButton = (Button) solo.getView(R.id.loginButtonDefaultLoginFragment);
        solo.enterText(usernameInput, "kristine");
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

        // click leave group
        Button leaveGroup = (Button) solo.getView(R.id.showGroups_b_leaveGroup);
        assertNotNull(leaveGroup);
        solo.clickOnView(leaveGroup);
        solo.sleep(TIME_OUT);
        assertFalse(solo.waitForText("LEAVE GROUP"));

        // and log out
        solo.clickOnMenuItem("Log out");
        solo.sleep(TIME_OUT);
        solo.assertCurrentActivity("Wrong activity", LoginActivity.class);

    }

}
