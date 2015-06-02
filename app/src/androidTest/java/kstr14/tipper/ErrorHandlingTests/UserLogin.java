package kstr14.tipper.ErrorHandlingTests;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;

import com.robotium.solo.Solo;

import kstr14.tipper.Activities.LoginActivity;
import kstr14.tipper.R;

/**
 * Created by Kristine on 02-06-2015.
 */
public class UserLogin extends ActivityInstrumentationTestCase2<LoginActivity> {

    private static final int TIME_OUT = 2000;
    private Solo solo;

    private EditText usernameInput;
    private EditText passwordInput;
    private Button loginButton;

    public UserLogin() {
        super(LoginActivity.class);
    }

    @Override
    public void setUp() {
        solo = new Solo(getInstrumentation(), getActivity());
        solo.assertCurrentActivity("Wrong activity", LoginActivity.class);

        usernameInput = (EditText) solo.getView(R.id.usernameDefaultLoginFragment);
        passwordInput = (EditText) solo.getView(R.id.passwordDefaultLoginFragment);
        loginButton = (Button) solo.getView(R.id.loginButtonDefaultLoginFragment);
    }

    @Override
    public void tearDown() {
        solo.finishOpenedActivities();
    }

    /**
     * Tests appropriate Toast is shown when username is wrong
     */
    public void testWrongUsername() {
        solo.enterText(usernameInput, "wrongusername");
        solo.enterText(passwordInput, "password");

        solo.clickOnView(loginButton);
        assertTrue(solo.waitForText("Login failed: Wrong password or username."));
    }

    /**
     * Tests appropriate Toast is shown when password is wrong
     */
    public void testWrongPassword() {
        solo.enterText(usernameInput, "TestUser1");
        solo.enterText(passwordInput, "wrongpassword");

        solo.clickOnView(loginButton);
        assertTrue(solo.waitForText("Login failed: Wrong password or username."));
    }
}
