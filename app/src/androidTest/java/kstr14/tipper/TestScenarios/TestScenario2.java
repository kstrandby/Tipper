package kstr14.tipper.TestScenarios;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.robotium.solo.Solo;

import kstr14.tipper.Activities.ListActivity;
import kstr14.tipper.Activities.LoginActivity;
import kstr14.tipper.Activities.MainActivity;
import kstr14.tipper.Activities.SearchTipActivity;
import kstr14.tipper.Activities.ShowTipActivity;
import kstr14.tipper.Data.Tip;
import kstr14.tipper.R;

/**
 * Created by Kristine on 02-06-2015.
 */
public class TestScenario2 extends ActivityInstrumentationTestCase2<LoginActivity> {

    private static final int TIME_OUT = 2000;
    private Solo solo;

    public TestScenario2() {
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
     *  - User performs a search for tips containing the word "free"
     *  - User clicks the first tip in the search result
     *  - User favourites the tip
     *  - User checks his favourites list and confirms the tip is in there
     *  - User logs out
     */
    public void testScenario2() {
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

        // click the search button in action bar and check that we get to SearchTipActivity
        assertTrue(solo.waitForView(solo.getCurrentActivity().findViewById(R.id.main_menu_search), TIME_OUT, false));
        solo.clickOnView(solo.getCurrentActivity().findViewById(R.id.main_menu_search));
        solo.assertCurrentActivity("Wrong activity", SearchTipActivity.class);

        // enter query and search for tips
        EditText keywordInput = (EditText) solo.getCurrentActivity().findViewById(R.id.searchTip_ed_keywords);
        solo.enterText(keywordInput, "free");
        CheckBox drinksCheckBox = (CheckBox) solo.getCurrentActivity().findViewById(R.id.searchTip_cb_drinks);
        CheckBox otherCheckBox = (CheckBox) solo.getCurrentActivity().findViewById(R.id.searchTip_cb_other);
        solo.clickOnView(drinksCheckBox);
        solo.clickOnView(otherCheckBox);
        Button searchButton = (Button) solo.getCurrentActivity().findViewById(R.id.searchTip_b_search);
        solo.clickOnView(searchButton);
        solo.sleep(TIME_OUT);
        solo.assertCurrentActivity("Wrong activity", ListActivity.class);

        // check that we have search results in ListView and click the first one
        solo.sleep(TIME_OUT);
        ListView listView = (ListView) solo.getCurrentActivity().findViewById(R.id.searchResult_lv_result);
        assertNotNull(listView);
        assertTrue(listView.getAdapter().getCount() > 0);
        solo.clickInList(1);
        solo.sleep(TIME_OUT);
        solo.assertCurrentActivity("Wrong activity", ShowTipActivity.class);
        ImageButton favouritesButton = (ImageButton) solo.getCurrentActivity().findViewById(R.id.showTip_ib_favourites);
        solo.clickOnView(favouritesButton);
        assertTrue(solo.waitForText("Tip added to favourites."));

        // go to favourites and check that the tip is there
        solo.clickOnMenuItem("Favourites");
        solo.sleep(TIME_OUT);
        solo.assertCurrentActivity("Wrong activity", ListActivity.class);
        solo.sleep(TIME_OUT*2);
        listView = (ListView) solo.getCurrentActivity().findViewById(R.id.searchResult_lv_result);
        assertNotNull(listView);
        assertTrue(listView.getAdapter().getCount() > 0);
        Tip tip = (Tip) listView.getAdapter().getItem(0);
        assertTrue(tip.getTitle().toLowerCase().contains("free"));

        // and log out
        solo.clickOnMenuItem("Log out");
    }
}
