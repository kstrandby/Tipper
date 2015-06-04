package kstr14.tipper;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.ImageButton;
import android.widget.ListView;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.robotium.solo.Solo;

import kstr14.tipper.Activities.ListActivity;
import kstr14.tipper.Activities.MainActivity;
import kstr14.tipper.Activities.ShowTipActivity;
import kstr14.tipper.Data.TipperUser;

/**
 * Created by Kristine on 01-06-2015.
 */
public class MainActivityTests extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;

    private ListView listView;
    private ImageButton foodButton;
    private ImageButton drinksButton;
    private ImageButton otherButton;

    public MainActivityTests() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
        ParseQuery query = ParseQuery.getQuery("TipperUser");
        ParseQuery parseQuery = query;
        parseQuery.whereEqualTo("username", "kristine");
        TipperUser user = (TipperUser) parseQuery.getFirst();
        assertNotNull(user);
        ((Application) getActivity().getApplicationContext()).setCurrentUser(user);
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    public void testButtonClicks() {
        // check we are in right activity
        solo.assertCurrentActivity("Wrong activity", MainActivity.class);

        int foodButtonID = R.id.main_ib_food;
        int drinksButtonID = R.id.main_ib_drinks;
        int otherButtonID = R.id.main_ib_other;

        // test click on foodButton results in correct activity
        foodButton = (ImageButton) solo.getView(foodButtonID);
        assertNotNull(foodButton);

        solo.clickOnView(foodButton);
        solo.assertCurrentActivity("Wrong activity", ListActivity.class);

        // go back and test click on drinksButton results in correct activity
        solo.goBack();
        drinksButton = (ImageButton) solo.getView(drinksButtonID);
        assertNotNull(drinksButton);

        solo.clickOnView(drinksButton);
        solo.assertCurrentActivity("Wrong activity", ListActivity.class);

        // go back and test click on otherButton results in correct activity
        solo.goBack();
        otherButton = (ImageButton) solo.getView(otherButtonID);
        assertNotNull(otherButton);

        solo.clickOnView(otherButton);
        solo.assertCurrentActivity("Wrong activity", ListActivity.class);
    }

    public void testListView() throws ParseException {
        // check we are in right activity
        solo.assertCurrentActivity("Wrong activity", MainActivity.class);

        int listViewID = R.id.main_lv_tips;
        listView = (ListView) solo.getView(listViewID);
        assertNotNull(listView);

        // click on all items in list, check they result in correct activity
        for(int i = 0; i < listView.getAdapter().getCount(); i++) {
            solo.clickInList(i + 1);
            solo.assertCurrentActivity("Wrong activity", ShowTipActivity.class);
            solo.goBack();
        }

        // long click on all items in list, check they result in alertdialog
        for(int i = 0; i < listView.getAdapter().getCount(); i++) {
            solo.clickLongInList(i + 1);
            assertTrue("Could not find the dialog!", solo.searchText("Remove tip?"));
            solo.clickOnButton("Cancel");
            assertFalse("The dialog is still showing", solo.searchText("Remove tip?"));
        }
    }
}
