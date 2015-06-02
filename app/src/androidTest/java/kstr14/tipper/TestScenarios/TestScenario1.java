package kstr14.tipper.TestScenarios;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseException;
import com.robotium.solo.Solo;

import java.util.Calendar;
import java.util.TimeZone;

import kstr14.tipper.Activities.CreateTipActivity;
import kstr14.tipper.Activities.LoginActivity;
import kstr14.tipper.Activities.MainActivity;
import kstr14.tipper.Activities.ShowTipActivity;
import kstr14.tipper.Data.Tip;
import kstr14.tipper.R;

/**
 * Created by Kristine on 02-06-2015.
 */
public class TestScenario1 extends ActivityInstrumentationTestCase2<LoginActivity> {

    private static final int TIME_OUT = 2000;

    private Solo solo;

    public TestScenario1() {
        super(LoginActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    /**
     * Tests the following scenario:
     *  - User logs in to the app
     *  - User creates a new tip
     *  - User clicks on the newly created tip to view it
     *  - User deletes the tip
     *  - User logs out of the app
     */
    public void testScenario1() throws ParseException {
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
        solo.sleep(TIME_OUT*2);

        // record current number of tips
        ListView listView = (ListView) solo.getCurrentActivity().findViewById(R.id.main_lv_tips);
        int numberOfTips = listView.getAdapter().getCount();

        // click on add button on actionBar and check we get to CreateTipActivity
        assertTrue(solo.waitForView(solo.getCurrentActivity().findViewById(R.id.main_action_add_tip), TIME_OUT, false));
        solo.clickOnView(solo.getCurrentActivity().findViewById(R.id.main_action_add_tip));
        solo.assertCurrentActivity("Wrong activity", CreateTipActivity.class);

        // set title and description
        EditText titleInput = (EditText) solo.getView(R.id.createTip_ed_title);
        solo.enterText(titleInput, "Free BBQ");
        EditText descriptionInput = (EditText) solo.getView(R.id.createTip_ed_description);
        solo.enterText(descriptionInput, "Delicious sausages on toast bread");

        // get current date
        TimeZone timeZone = TimeZone.getTimeZone("Australia/Melbourne");
        Calendar current = Calendar.getInstance(timeZone);

        // set end date to a week from current date at 14
        solo.clickOnText("End:");
        solo.setDatePicker(0, current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH) + 7);
        solo.clickOnText("OK");
        solo.setTimePicker(0, 10, 0);
        solo.clickOnText("OK");

        // set start date a week from current date at 12
        solo.clickOnText("Start:");
        solo.setDatePicker(0, current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH) + 7);
        solo.clickOnText("OK");
        solo.setTimePicker(0, 8, 0);
        solo.clickOnText("OK");

        // click create and check that we are back to MainActivity
        Button createButton = (Button) solo.getView(R.id.createTip_b_create);
        solo.clickOnView(createButton);
        solo.assertCurrentActivity("Wrong activity", MainActivity.class);

        // check that number of tips are now 1 more than before
        solo.sleep(TIME_OUT);
        int newNumberOfTips = listView.getAdapter().getCount();
        assertEquals(numberOfTips + 1, newNumberOfTips);

        // check that the new Tip is shown in listView
        Tip tip = (Tip) listView.getAdapter().getItem(newNumberOfTips- 1);
        assertEquals("Free BBQ", tip.getTitle());

        // click on newly created Tip and check that we go to ShowTipActivity
        solo.scrollListToBottom(listView);
        solo.clickInList(newNumberOfTips);
        solo.sleep(TIME_OUT);
        solo.assertCurrentActivity("Wrong activity", ShowTipActivity.class);

        // check the attributes of tip are correct
        TextView descriptionView = (TextView) solo.getView(R.id.showTip_tv_description);
        assertEquals("Delicious sausages on toast bread", descriptionView.getText());
        TextView upVotesView = (TextView) solo.getView(R.id.showTip_tv_upvotes);
        assertEquals("0", upVotesView.getText());
        TextView downVotesView = (TextView) solo.getView(R.id.showTip_tv_downvotes);
        assertEquals("0", downVotesView.getText());

        // go back and delete the tip
        solo.goBack();
        solo.assertCurrentActivity("Wrong activity", MainActivity.class);
        solo.clickLongInList(newNumberOfTips);
        assertTrue("Could not find the dialog!", solo.searchText("Remove tip?"));
        solo.clickOnButton("OK");
        solo.waitForText("Tip has been deleted.");
        solo.sleep(TIME_OUT);

        // and log out
        solo.clickOnMenuItem("Log out");
        solo.sleep(TIME_OUT);
        solo.assertCurrentActivity("Wrong activity", LoginActivity.class);
    }
}
