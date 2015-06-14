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
public class UserSignup extends ActivityInstrumentationTestCase2<LoginActivity> {

    private Solo solo;

    private Button signUpButtonDefault;
    private EditText usernameInput;
    private EditText emailInput;
    private EditText passwordInput1;
    private EditText passwordInput2;
    private Button signUpButton;

    public UserSignup() {
        super(LoginActivity.class);
    }

    @Override
    public void setUp() {
        solo = new Solo(getInstrumentation(), getActivity());
        solo.assertCurrentActivity("Wrong activity", LoginActivity.class);

        signUpButtonDefault = (Button) solo.getView(R.id.signUpDefaultLoginFragment);
        assertNotNull(signUpButtonDefault);
        solo.clickOnView(signUpButtonDefault);

        usernameInput = (EditText) solo.getView(R.id.usernameSignupFragment);
        assertNotNull(usernameInput);
        emailInput = (EditText) solo.getView(R.id.emailSignupFragment);
        assertNotNull(emailInput);
        passwordInput1 = (EditText) solo.getView(R.id.passwordSignupFragment);
        assertNotNull(passwordInput1);
        passwordInput2 = (EditText) solo.getView(R.id.reenterPasswordSignupFragment);
        assertNotNull(passwordInput2);
        signUpButton = (Button) solo.getView(R.id.signupButtonSignupFragment);
    }

    @Override
    public void tearDown() {
        solo.finishOpenedActivities();
    }

    /**
     * Tests that Toast with appropriate message is shown when user
     * attempts to create a user account with an username that has already been taken
     */
    public void testTakenUsername() {
        solo.enterText(usernameInput, "TestUser1");
        solo.enterText(emailInput, "some@email.com");
        solo.enterText(passwordInput1, "password");
        solo.enterText(passwordInput2, "password");
        solo.clickOnView(signUpButton);

        assertTrue(solo.waitForText("Sorry, username already taken."));
        solo.goBack();
    }

    /**
     * Tests that Toast with appropriate message is shown when user enters two
     * different passwords when signing up
     */
    public void testPasswordsDoNotMatch() {
        solo.enterText(usernameInput, "TestUser2");
        solo.enterText(emailInput, "some@email.com");
        solo.enterText(passwordInput1, "password");
        solo.enterText(passwordInput2, "differentpassword");
        solo.clickOnView(signUpButton);

        assertTrue(solo.waitForText("Passwords do not match, try again."));
        solo.goBack();
    }


    /**
     * Tests that Toast with appropriate message is shown when user enters
     * a non-valid email address when signing up
     */
    public void testNonValidEmail() {
        solo.enterText(usernameInput, "TestUser2");
        solo.enterText(emailInput, "nonvalidemail");
        solo.enterText(passwordInput1, "password");
        solo.enterText(passwordInput2, "password");
        solo.clickOnView(signUpButton);

        assertTrue(solo.waitForText("Please enter a valid email."));
        solo.goBack();
    }

    /**
     * Tests that Toast with appropriate message is shown when user enters
     * an email which already exist in the database, when signing up
     */
    public void testAccountWithEmailAlreadyExists() {
        solo.enterText(usernameInput, "TestUser2");
        solo.enterText(emailInput, "testuser1@tipper.com");
        solo.enterText(passwordInput1, "password");
        solo.enterText(passwordInput2, "password");
        solo.clickOnView(signUpButton);

        assertTrue(solo.waitForText("Account already exists with this email."));
        solo.goBack();
    }

    /**
     * Tests that Toast with appropriate message is shown when user enters a
     * password which is too short (below 8 characters), when signing up
     */
    public void testPasswordTooShort() {
        solo.enterText(usernameInput, "TestUser2");
        solo.enterText(emailInput, "testuser2@tipper.com");
        solo.enterText(passwordInput1, "1234");
        solo.enterText(passwordInput2, "1234");
        solo.clickOnView(signUpButton);

        assertTrue(solo.waitForText("Password too short - must be minimum 8 characters."));
        solo.goBack();
    }
}
