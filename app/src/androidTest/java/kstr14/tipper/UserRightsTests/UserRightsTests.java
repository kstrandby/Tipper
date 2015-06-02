package kstr14.tipper.UserRightsTests;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.robotium.solo.Solo;

import java.util.Calendar;
import java.util.TimeZone;

import kstr14.tipper.Activities.CreateTipActivity;
import kstr14.tipper.Activities.LoginActivity;
import kstr14.tipper.Activities.MainActivity;
import kstr14.tipper.Activities.ShowTipActivity;
import kstr14.tipper.R;

/**
 * Created by Kristine on 02-06-2015.
 */
public class UserRightsTests extends ActivityInstrumentationTestCase2<LoginActivity> {

    private Solo solo;
    private static final int TIME_OUT = 2000;

    public UserRightsTests() {
        super(LoginActivity.class);
    }

    @Override
    public void setUp() {
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
    public void tearDown() {
        solo.clickOnMenuItem("Log out");
        solo.sleep(TIME_OUT);
        solo.assertCurrentActivity("Wrong activity", LoginActivity.class);
        solo.finishOpenedActivities();
    }

    /**
     * Tests a successful creation of a Tip and that the creator of the Tip cannot
     * upvote or downvote for his own tip
     */
    public void testUpvoteDownvoteOwnTip() {
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

        solo.sleep(TIME_OUT);
        ListView listView = (ListView) solo.getCurrentActivity().findViewById(R.id.main_lv_tips);
        assertNotNull(listView);

        // click on newly created Tip and check that we go to ShowTipActivity
        solo.scrollListToBottom(listView);
        solo.clickInList(listView.getAdapter().getCount());
        solo.sleep(TIME_OUT);
        solo.assertCurrentActivity("Wrong activity", ShowTipActivity.class);

        // check clicks on upvote and downvote is not possible
        ImageButton upVoteButton = (ImageButton) solo.getView(R.id.showTip_ib_upvote);
        TextView upvotesView = (TextView) solo.getView(R.id.showTip_tv_upvotes);
        int upvotes = Integer.parseInt(upvotesView.getText().toString());
        solo.clickOnView(upVoteButton);
        assertTrue(solo.waitForText("You cannot upvote your own tip."));
        assertEquals(upvotes, Integer.parseInt(upvotesView.getText().toString()));

        ImageButton downVoteButton = (ImageButton) solo.getView(R.id.showTip_ib_downvote);
        TextView downvotesView = (TextView) solo.getView(R.id.showTip_tv_downvotes);
        int downvotes = Integer.parseInt(downvotesView.getText().toString());
        solo.clickOnView(downVoteButton);
        assertTrue(solo.waitForText("You cannot downvote your own tip."));
        assertEquals(downvotes, Integer.parseInt(downvotesView.getText().toString()));

        // go back
        solo.goBack();
        solo.assertCurrentActivity("Wrong activity", MainActivity.class);
    }

    /**
     * Tests that the creator of a Tip cannot delete the Tip
     */
    public void testDeleteTipWithoutRights() {
        // create a tip as current user
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

        // now log out and log in as another user
        solo.clickOnMenuItem("Log out");
        solo.sleep(TIME_OUT);
        solo.assertCurrentActivity("Wrong activity", LoginActivity.class);
        solo.sleep(TIME_OUT);
        EditText usernameInput = (EditText) solo.getView(R.id.usernameDefaultLoginFragment);
        EditText passwordInput = (EditText) solo.getView(R.id.passwordDefaultLoginFragment);
        solo.enterText(usernameInput, "TestUser1");
        solo.enterText(passwordInput, "password");
        Button loginButton = (Button) solo.getView(R.id.loginButtonDefaultLoginFragment);
        solo.clickOnView(loginButton);
        solo.sleep(TIME_OUT);
        solo.assertCurrentActivity("Wrong activity", MainActivity.class);
        solo.sleep(TIME_OUT*2);

        // try to delete the tip and check Toast is shown and Tip is not deleted
        ListView listView = (ListView) solo.getCurrentActivity().findViewById(R.id.main_lv_tips);
        assertNotNull(listView);
        int numberOfTips = listView.getAdapter().getCount();
        solo.clickLongInList(numberOfTips);
        assertTrue(solo.waitForText("You do not have the rights to delete this tip."));
        assertEquals(numberOfTips, listView.getAdapter().getCount());
    }
}
