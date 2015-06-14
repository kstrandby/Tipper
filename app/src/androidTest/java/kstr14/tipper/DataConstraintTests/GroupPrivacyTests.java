package kstr14.tipper.DataConstraintTests;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.robotium.solo.Solo;

import java.util.ArrayList;

import kstr14.tipper.Activities.CreateTipActivity;
import kstr14.tipper.Activities.LoginActivity;
import kstr14.tipper.Activities.MainActivity;
import kstr14.tipper.R;

/**
 * Created by Kristine on 02-06-2015.
 */
public class GroupPrivacyTests extends ActivityInstrumentationTestCase2<LoginActivity> {

    private static final int TIME_OUT = 2000;
    private Solo solo;

    private EditText titleInput;
    private Button createButton;
    private ListView listView;

    private int initialNumberOfTips;

    public GroupPrivacyTests() {
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
        solo.enterText(usernameInput, "TestUser1");
        solo.enterText(passwordInput, "password");
        solo.clickOnView(loginButton);

        // test we are in MainActivity and wait TIME_OUT value until tips are loaded
        solo.sleep(TIME_OUT);
        solo.assertCurrentActivity("Wrong activity", MainActivity.class);
        solo.sleep(TIME_OUT * 2);

        // record initial number of tips in ListView
        listView = (ListView) solo.getCurrentActivity().findViewById(R.id.main_lv_tips);
        initialNumberOfTips = listView.getAdapter().getCount();
        assertTrue(solo.waitForView(solo.getCurrentActivity().findViewById(R.id.main_action_add_tip), TIME_OUT, false));

        // go to CreateTipActivity
        solo.clickOnView(solo.getCurrentActivity().findViewById(R.id.main_action_add_tip));
        solo.assertCurrentActivity("Wrong activity", CreateTipActivity.class);

        createButton = (Button) solo.getView(R.id.createTip_b_create);
        titleInput = (EditText) solo.getView(R.id.createTip_ed_title);

    }

    @Override
    public void tearDown() {
        solo.clickOnMenuItem("Log out");
        solo.sleep(TIME_OUT);
        solo.assertCurrentActivity("Wrong activity", LoginActivity.class);
        solo.finishOpenedActivities();
    }

    /**
     * Tests successful creation of a new Tip belonging to a closed group.
     * When a group is closed, tips of this group are not shown in MainActivity, so the
     * tests checks that this is the case after the "private" Tip has been created.
     */
    public void testPrivateTip() {
        // check we are in right activity and enter a title
        solo.assertCurrentActivity("Wrong activity", CreateTipActivity.class);
        solo.enterText(titleInput, "TestTitle");

        // find spinner and click Caulfield group (item number 1 in spinner list)
        View spinner = solo.getView(Spinner.class, 1);
        assertNotNull(spinner);
        solo.clickOnView(spinner);
        solo.clickInList(1);

        // attempt to click create button
        createButton = (Button) solo.getView(R.id.createTip_b_create);
        solo.clickOnView(createButton);

        // check we are back in MainActivity and that the number of items in ListView is the same as before
        solo.assertCurrentActivity("Wrong activity", MainActivity.class);
        assertEquals(initialNumberOfTips, listView.getAdapter().getCount());
    }

    /**
     * Tests successful creation of a Tip belonging to an open group. In this case the Tip
     * will be shown in the ListView in MainActivity, so the test also tests that this is the case.
     */
    public void testPublicTip() {
        // check we are in right activity and enter a title
        solo.assertCurrentActivity("Wrong activity", CreateTipActivity.class);
        solo.enterText(titleInput, "TestTitle");

        // find spinner and click public group (item number 2 in spinner list)
        ArrayList<Spinner> currentSpinners = solo.getCurrentViews(Spinner.class);
        assertEquals(2, currentSpinners.size());
        solo.clickOnView(solo.getCurrentActivity().findViewById(R.id.createTip_s_groupChoice));
        solo.clickInList(2);

        // attempt to click create button
        createButton = (Button) solo.getView(R.id.createTip_b_create);
        solo.clickOnView(createButton);

        // check we are back in MainActivity and that the number of items in ListView is one more than before
        solo.sleep(TIME_OUT);
        solo.assertCurrentActivity("Wrong activity", MainActivity.class);
        assertEquals(initialNumberOfTips + 1, listView.getAdapter().getCount());
    }

}
