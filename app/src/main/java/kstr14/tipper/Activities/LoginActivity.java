package kstr14.tipper.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import kstr14.tipper.Application;
import kstr14.tipper.Data.TipperUser;
import kstr14.tipper.R;


public class LoginActivity extends ActionBarActivity {

    private static final String ACTIVITY_ID = "LoginActivity";

    // UI elements for default login fragment
    private EditText usernameDefaultLogin;
    private EditText passwordDefaultLogin;

    // UI elements for sign up fragment
    private EditText usernameSignup;
    private EditText emailSignup;
    private EditText passwordSignup;
    private EditText reenterPasswordSignup;

    private Application app;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        app = ((Application) getApplicationContext());

        // check for cached user, and go directly to MainActivity if found
        ParseQuery<ParseObject> query = ParseQuery.getQuery("TipperUser");
        try {
            TipperUser user = (TipperUser) query.fromLocalDatastore().getFirst();
            if (user != null) {
                Log.d(ACTIVITY_ID, "Found cached user: " + user.getUsername());
                app.setCurrentUser(user);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Log.d(ACTIVITY_ID, "No cached user found.");
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        // set fragment to the default login screen
        DefaultLoginFragment defaultLoginFragment = new DefaultLoginFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.LoginActivity_fragment_container, defaultLoginFragment).commit();

    }


    // Required for Facebook and Google login callbacks
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DefaultLoginFragment fragment = (DefaultLoginFragment) getSupportFragmentManager()
                .findFragmentById(R.id.LoginActivity_fragment_container);
        fragment.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Method called when sign up button pressed on the default login fragment
     * Switches the default login fragment with a sign up fragment
     *
     * @param view
     */
    public void defaultSignUpPressed(View view) {
        SignUpFragment signUpFragment = new SignUpFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Replace the default login fragment with the sign up fragment,
        // and add the transaction to the back stack so the user can navigate back
        fragmentTransaction.replace(R.id.LoginActivity_fragment_container, signUpFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    /**
     * Method called when login button pressed on the default login fragment
     * Attempts to log in the user, if successful goes to MainActivity
     *
     * @param view
     */
    public void defaultLoginPressed(View view) {
        // initialize UI elements for default login fragment
        usernameDefaultLogin = (EditText) findViewById(R.id.usernameDefaultLoginFragment);
        passwordDefaultLogin = (EditText) findViewById(R.id.passwordDefaultLoginFragment);

        // fetch input and attempt login
        final String username = usernameDefaultLogin.getText().toString();
        final String password = passwordDefaultLogin.getText().toString();

        ParseQuery<TipperUser> query = ParseQuery.getQuery("TipperUser");
        query.whereEqualTo("username", username);
        query.findInBackground(new FindCallback<TipperUser>() {
            @Override
            public void done(List<TipperUser> list, ParseException e) {
                if (!list.isEmpty()) {
                    TipperUser user = list.get(0);
                    if (BCrypt.checkpw(password, user.getPassword())) {
                        app.setCurrentUser(user);
                        user.pinInBackground();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), "Login failed: Wrong password or username.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Login failed: Wrong password or username.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Method called when sign up button pressed in sign up fragment
     * Attempts to register the user, if successful goes to MainActivity
     *
     * @param view
     */
    public void signupPressed(View view) {
        // fetch the values
        usernameSignup = (EditText) findViewById(R.id.usernameSignupFragment);
        emailSignup = (EditText) findViewById(R.id.emailSignupFragment);
        passwordSignup = (EditText) findViewById(R.id.passwordSignupFragment);
        reenterPasswordSignup = (EditText) findViewById(R.id.reenterPasswordSignupFragment);

        String username = usernameSignup.getText().toString();
        String email = emailSignup.getText().toString();
        String password1 = passwordSignup.getText().toString();
        String password2 = reenterPasswordSignup.getText().toString();

        // validation
        if (username.length() == 0) {
            Toast.makeText(getApplicationContext(), "Please enter a username.", Toast.LENGTH_SHORT).show();
        } else if (password1.length() == 0) {
            Toast.makeText(getApplicationContext(), "Please enter a password.", Toast.LENGTH_SHORT).show();
        } else if (!validatePassword(password1, password2)) {
            Toast.makeText(getApplicationContext(), "Passwords do not match, try again.", Toast.LENGTH_SHORT).show();
        } else if (!passwordLongEnough(password1)) {
            Toast.makeText(getApplicationContext(), "Password too short - must be minimum 8 characters.", Toast.LENGTH_SHORT).show();
        } else try {
            if (emailAlreadyExists(email)) {
                Toast.makeText(getApplicationContext(), "Account already exists with this email.", Toast.LENGTH_SHORT).show();
            } else if (!validateEmail(email)) {
                Toast.makeText(getApplicationContext(), "Please enter a valid email.", Toast.LENGTH_SHORT).show();
            } else try {
                if (!usernameAvailable(username)) {
                    Toast.makeText(getApplicationContext(), "Sorry, username already taken.", Toast.LENGTH_SHORT).show();
                } else {

                    // setup a user object with the given attributes, save and enter the app
                    final TipperUser user = new TipperUser();
                    user.setUsername(username);

                    // hash password with salt
                    String hashed = BCrypt.hashpw(password1, BCrypt.gensalt());
                    user.setPassword(hashed);

                    user.setEmail(email);
                    user.setGoogleUser(false);
                    user.setFacebookUser(false);
                    user.save();
                    app.setCurrentUser(user);
                    user.pinInBackground();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Validates that a password is longer than 8 characters
     *
     * @param password, the password to be validated
     * @return true if the password is long enough, false otherwise
     */
    public static boolean passwordLongEnough(String password) {
        if (password.length() < 8) return false;
        else return true;
    }

    /**
     * Validates that two passwords are equal
     *
     * @param password1, the first password
     * @param password2, the second password
     * @return true  if they are equal, false otherwise
     */
    public static boolean validatePassword(String password1, String password2) {
        if (password1.equals(password2)) return true;
        else return false;
    }

    /**
     * Validates the structure of an email address
     *
     * @param email, the email to be validated
     * @return true if the structure is correct, false otherwise
     */
    public boolean validateEmail(String email) {
        boolean result = true;
        try {
            InternetAddress internetAddress = new InternetAddress(email);
            internetAddress.validate();
        } catch (AddressException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    /**
     * Checks if a username is available by looking up in the database, if
     * there already exists an entity with that username
     *
     * @param username, the username to be checked for availability
     * @return true if the username is available, false otherwise
     * @throws ParseException
     */
    private boolean usernameAvailable(String username) throws ParseException {
        ParseQuery<TipperUser> query = ParseQuery.getQuery("TipperUser");
        query.whereEqualTo("username", username);
        List<TipperUser> result = query.find();
        if (result.isEmpty()) {
            return true;
        } else return false;
    }

    /**
     * Checks if an account with the given email already exists by looking the email up
     * in the database
     *
     * @param email, the email to be checked
     * @return true if an account already exists with the specified email, false otherwise
     * @throws ParseException
     */
    private boolean emailAlreadyExists(String email) throws ParseException {
        ParseQuery<TipperUser> query = ParseQuery.getQuery("TipperUser");
        query.whereEqualTo("email", email);
        List<TipperUser> result = query.find();
        if (result.isEmpty()) {
            return false;
        } else return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    /**
     * Exits the app on back button click
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
