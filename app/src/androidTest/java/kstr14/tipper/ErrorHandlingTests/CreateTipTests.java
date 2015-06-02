package kstr14.tipper.ErrorHandlingTests;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;

import com.robotium.solo.Solo;

import java.util.Calendar;
import java.util.TimeZone;

import kstr14.tipper.Activities.CreateTipActivity;
import kstr14.tipper.Activities.LoginActivity;
import kstr14.tipper.Activities.MainActivity;
import kstr14.tipper.R;

/**
 * Created by Kristine on 02-06-2015.
 */
public class CreateTipTests extends ActivityInstrumentationTestCase2<LoginActivity> {

    private static final int TIME_OUT = 2000;

    private Solo solo;
    private EditText titleInput;
    private Button createButton;

    public CreateTipTests() {

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

        assertTrue(solo.waitForView(solo.getCurrentActivity().findViewById(R.id.main_action_add_tip), TIME_OUT, false));

        // go to CreateTipActivity
        solo.clickOnView(solo.getCurrentActivity().findViewById(R.id.main_action_add_tip));
        solo.assertCurrentActivity("Wrong activity", CreateTipActivity.class);

        createButton = (Button) solo.getView(R.id.createTip_b_create);
        titleInput = (EditText) solo.getView(R.id.createTip_ed_title);

    }

    @Override
    public void tearDown() throws Exception {
        solo.clickOnMenuItem("Log out");
        solo.sleep(TIME_OUT);
        solo.assertCurrentActivity("Wrong activity", LoginActivity.class);
        solo.finishOpenedActivities();
    }

    /**
     * Tests that a Toast is shown with appropriate text, when user tries to
     * create a Tip with no title
     */
    public void testNoTitle() {
        // test Toast is shown with the correct text
        solo.clickOnView(createButton);
        assertTrue(solo.waitForText("Please input a title."));

        // test that we are still in CreateTipActivity
        solo.assertCurrentActivity("Wrong activity", CreateTipActivity.class);

        solo.goBack();
    }

    /**
     * Tests that a Toast is shown with appropriate text, when user tries to
     * create a Tip with end date before start date
     */
    public void testEndDateBeforeStartDate() {
        // check we are in right activity
        solo.assertCurrentActivity("Wrong activity", CreateTipActivity.class);

        solo.enterText(titleInput, "TestTitle");

        // get current date
        TimeZone timeZone = TimeZone.getTimeZone("Australia/Melbourne");
        Calendar current = Calendar.getInstance(timeZone);

        // set end date to 2 days from current date 10 AM
        solo.clickOnText("End:");
        solo.setDatePicker(0, current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH) + 1);
        solo.clickOnText("OK");
        solo.setTimePicker(0, 10, 0);
        solo.clickOnText("OK");

        // set start date 1 day before end date 10 AM
        solo.clickOnText("Start:");
        solo.setDatePicker(0, current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH) + 2);
        solo.clickOnText("OK");
        solo.setTimePicker(0, 10, 0);
        solo.clickOnText("OK");

        // attempt to click create button
        createButton = (Button) solo.getView(R.id.createTip_b_create);
        solo.clickOnView(createButton);
        assertTrue(solo.waitForText("End date cannot be before start date."));

        // check we are still in CreateTipActivity
        solo.assertCurrentActivity("Wrong activity", CreateTipActivity.class);

        solo.goBack();
    }

    /**
     * Tests that a Toast is shown with appropriate text, when a user attempts to
     * create a Tip with dates before current date
     */
    public void testStartDateBeforeCurrentDate() {
        // check we are in right activity
        solo.assertCurrentActivity("Wrong activity", CreateTipActivity.class);

        titleInput = (EditText) solo.getView(R.id.createTip_ed_title);
        solo.enterText(titleInput, "TestTitle");

        // get current date
        TimeZone timeZone = TimeZone.getTimeZone("Australia/Melbourne");
        Calendar current = Calendar.getInstance(timeZone);

        // set end date to yesterday 10 AM
        solo.clickOnText("End:");
        solo.setDatePicker(0, current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH) - 1);
        solo.clickOnText("OK");
        solo.setTimePicker(0, 10, 0);
        solo.clickOnText("OK");

        // set start date to yesterday 8 AM
        solo.clickOnText("Start:");
        solo.setDatePicker(0, current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH) - 1);
        solo.clickOnText("OK");
        solo.setTimePicker(0, 8, 0);
        solo.clickOnText("OK");

        // attempt to click create button
        createButton = (Button) solo.getView(R.id.createTip_b_create);
        solo.clickOnView(createButton);
        assertTrue(solo.waitForText("Please input a valid start date."));

        // check we are still in CreateTipActivity
        solo.assertCurrentActivity("Wrong activity", CreateTipActivity.class);

        solo.goBack();
    }

}


