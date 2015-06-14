package kstr14.tipper.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.json.JSONObject;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import kstr14.tipper.Application;
import kstr14.tipper.Data.TipperUser;
import kstr14.tipper.ImageHelper;
import kstr14.tipper.R;

/**
 * Fragment for showing default login UI containing fields for username and password
 * along with buttons for login, sign up as well as Google+ and Facebook login
 */
public class DefaultLoginFragment extends Fragment
        implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // ACTIVITY_ID is used for logging and keeping track of navigation between activities
    private static final String ACTIVITY_ID = "LoginActivity";

    public static final int GOOGLE_SIGN_IN = 1;

    // UI elements
    private ImageView tipper;
    private Bitmap tipperBitmap;
    private LoginButton facebookButton;
    private SignInButton googleButton;
    private TextView forgotPassword;
    private CallbackManager callbackManager;

    private Activity loginActivity;
    private Context context;

    // Google+ sign in
    private GoogleApiClient googleApiClient;
    private boolean googleSignInClicked;
    private boolean intentInProgress;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        loginActivity = getActivity();
        context = getActivity().getApplicationContext();
        FacebookSdk.sdkInitialize(context);
        callbackManager = CallbackManager.Factory.create();

        // setup UI elements
        View view = inflater.inflate(R.layout.fragment_default_login, container, false);
        view.findViewById(R.id.google_sign_in_button).setOnClickListener(this);
        tipper = (ImageView) view.findViewById(R.id.app_logo);

        tipperBitmap = ImageHelper.decodeBitmapFromResource(getResources(), R.drawable.tipper, 256, 256);
        tipper.setImageBitmap(tipperBitmap);
        forgotPassword = (TextView) view.findViewById(R.id.forgotPasswordButtonDefaultLoginFragment);
        forgotPassword.setOnClickListener(this);

        // setup facebook login
        facebookButton = (LoginButton) view.findViewById(R.id.facebook_sign_in_button);
        facebookButton.setReadPermissions(Arrays.asList("public_profile", "email"));
        facebookButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(ACTIVITY_ID, loginResult.getAccessToken().getUserId() + " logged in");
                handleFacebookUser(loginResult);
            }

            @Override
            public void onCancel() {
                Log.d(ACTIVITY_ID, "Facebook login cancelled.");
            }

            @Override
            public void onError(FacebookException e) {
                Log.e(ACTIVITY_ID, "Facebook login failed: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // setup the google api and sign in button
        googleApiClient = new GoogleApiClient.Builder(view.getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();

        googleButton = (SignInButton) view.findViewById(R.id.google_sign_in_button);
        setGoogleButtonText(googleButton, "Log in with Google+");
        facebookButton.setFragment(this);

        return view;
    }

    /**
     * Finds the TextView inside the Google+ SignInButton and sets the tex
     *
     * @param signInButton
     * @param text
     */
    private void setGoogleButtonText(SignInButton signInButton, String text) {
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View view = signInButton.getChildAt(i);

            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                textView.setText(text);
                return;
            }
        }
    }

    /**
     * Handles GoogleApiClient connection failure
     *
     * @param result
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!intentInProgress) {
            if (googleSignInClicked && result.hasResolution()) {
                // Attempt to resolve errors
                try {
                    result.startResolutionForResult(loginActivity, GOOGLE_SIGN_IN);
                    intentInProgress = true;
                } catch (IntentSender.SendIntentException e) {
                    // The intent was canceled before it was sent.  Return to the default
                    // state and attempt to connect again
                    intentInProgress = false;
                    googleApiClient.connect();
                }
            }
        }
    }

    /**
     * Handles activity results after Google+ sign in and Facebook sign in
     *
     * @param requestCode
     * @param responseCode
     * @param intent
     */
    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == GOOGLE_SIGN_IN) {
            // google login
            if (responseCode != Activity.RESULT_OK) {
                googleSignInClicked = false;
            }
            intentInProgress = false;
            if (!googleApiClient.isConnected()) {
                googleApiClient.reconnect();
            }
        } else {
            // facebook login
            callbackManager.onActivityResult(requestCode, responseCode, intent);
        }
    }


    /**
     * Handles onClick events for Google+ sign in button and forgot password button
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.google_sign_in_button && !googleApiClient.isConnecting()) {
            // Google+ login button clicked
            googleSignInClicked = true;
            googleApiClient.connect();
        } else if (view.getId() == R.id.forgotPasswordButtonDefaultLoginFragment) {
            // forgot password clicked
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
            layoutParams.setMargins(50, 0, 50, 0);

            // set up AlertDialog for user to enter his/her email
            AlertDialog.Builder forgotPasswordDialog = new AlertDialog.Builder(loginActivity);
            forgotPasswordDialog.setTitle("Forgot password?");
            final TextView enterEmailText = new TextView(loginActivity);
            enterEmailText.setText("Please enter your email:");
            enterEmailText.setLayoutParams(layoutParams);
            final EditText emailInput = new EditText(loginActivity);
            emailInput.setInputType(InputType.TYPE_CLASS_TEXT);
            emailInput.setWidth(40);
            emailInput.setLayoutParams(layoutParams);
            LinearLayout linearLayout = new LinearLayout(loginActivity);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.addView(enterEmailText);
            linearLayout.addView(emailInput);
            forgotPasswordDialog.setView(linearLayout);
            forgotPasswordDialog.setPositiveButton("Send password", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // send email with password using cloud code function
                    final String email = emailInput.getText().toString();
                    System.out.println("Email: " + email);
                    HashMap<String, Object> params = new HashMap<String, Object>();
                    params.put("email", email);
                    ParseCloud.callFunctionInBackground("sendForgotPasswordEmail", params, new FunctionCallback<String>() {
                        @Override
                        public void done(String response, ParseException e) {
                            if (e == null) {
                                if (response.equals("Email sent!")) {
                                    Toast.makeText(context, response, Toast.LENGTH_SHORT).show();
                                    showEnterOneTimePasswordDialog(email);
                                }
                            } else {
                                Toast.makeText(context, "Error sending password reset email", Toast.LENGTH_SHORT).show();
                                Log.e(ACTIVITY_ID, "Parse error: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
            forgotPasswordDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            forgotPasswordDialog.show();
        }
    }

    /**
     * Shows a Dialog prompting the user for the one-time password just emailed to the user
     * Checks the entered one-time password with the database and in turn calls showEnterNewPasswordDialog,
     * if the password was correct
     *
     * @param email
     */
    public void showEnterOneTimePasswordDialog(final String email) {
        // set up Dialog
        AlertDialog.Builder enterOneTimePasswordDialog = new AlertDialog.Builder(loginActivity);
        LinearLayout linearLayout = new LinearLayout(loginActivity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        layoutParams.setMargins(50, 0, 50, 0);
        enterOneTimePasswordDialog.setTitle("Reset Password");
        TextView enterOneTimePasswordText = new TextView(loginActivity);
        enterOneTimePasswordText.setText("Enter one time password:");
        enterOneTimePasswordText.setLayoutParams(layoutParams);
        final EditText oneTimePasswordInput = new EditText(loginActivity);
        oneTimePasswordInput.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        oneTimePasswordInput.setLayoutParams(layoutParams);
        linearLayout.addView(enterOneTimePasswordText);
        linearLayout.addView(oneTimePasswordInput);
        enterOneTimePasswordDialog.setView(linearLayout);
        enterOneTimePasswordDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // check entered password with database
                final String enteredPassword = oneTimePasswordInput.getText().toString();
                ParseQuery<TipperUser> query = ParseQuery.getQuery("TipperUser");
                query.whereEqualTo("email", email);
                query.findInBackground(new FindCallback<TipperUser>() {
                    @Override
                    public void done(List<TipperUser> list, ParseException e) {
                        if (list != null && !list.isEmpty()) {
                            TipperUser user = list.get(0);
                            if (user.getOneTimePassword() != null && user.getOneTimePassword().equals(enteredPassword)) {
                                showEnterNewPasswordDialog(user);
                            } else {
                                Toast.makeText(context, "Entered one time password is not correct.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });
        enterOneTimePasswordDialog.show();
    }

    /**
     * Shows a Dialog prompting the user to enter a new password
     * Validates the entered password, saves it to the database if valid and sends
     * the user to MainActivity
     *
     * @param user
     */
    public void showEnterNewPasswordDialog(final TipperUser user) {
        // set up Dialog
        LinearLayout linearLayout = new LinearLayout(loginActivity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        layoutParams.setMargins(50, 0, 50, 0);
        TextView enterPassword1 = new TextView(loginActivity);
        enterPassword1.setText("Enter new password:");
        enterPassword1.setLayoutParams(layoutParams);
        TextView enterPassword2 = new TextView(loginActivity);
        enterPassword2.setText("Reenter password: ");
        enterPassword2.setLayoutParams(layoutParams);
        final EditText password1Input = new EditText(loginActivity);
        password1Input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password1Input.setLayoutParams(layoutParams);
        final EditText password2Input = new EditText(loginActivity);
        password2Input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password2Input.setLayoutParams(layoutParams);
        linearLayout.addView(enterPassword1);
        linearLayout.addView(password1Input);
        linearLayout.addView(enterPassword2);
        linearLayout.addView(password2Input);

        final AlertDialog enterNewPasswordDialog = new AlertDialog.Builder(loginActivity)
                .setTitle("Enter new password")
                .setView(linearLayout)
                .setPositiveButton("OK", null)
                .setNegativeButton("Cancel", null)
                .create();

        enterNewPasswordDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positive = enterNewPasswordDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negative = enterNewPasswordDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // input validation
                        String password1 = password1Input.getText().toString();
                        String password2 = password2Input.getText().toString();
                        if (!LoginActivity.validatePassword(password1, password2)) {
                            Toast.makeText(context, "Passwords do not match, try again.", Toast.LENGTH_SHORT).show();
                        } else if (!LoginActivity.passwordLongEnough(password1)) {
                            Toast.makeText(context, "Password too short - must be minimum 8 characters.", Toast.LENGTH_SHORT).show();
                        } else {
                            // save new password, dismiss Dialog and send the user to MainActivity
                            String hashed = BCrypt.hashpw(password1, BCrypt.gensalt());
                            user.setPassword(hashed);
                            user.saveInBackground();
                            Toast.makeText(context, "Password changed.", Toast.LENGTH_SHORT).show();
                            enterNewPasswordDialog.dismiss();
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.putExtra("uuid", user.getUuidString());
                            startActivity(intent);
                        }
                    }
                });
                negative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        enterNewPasswordDialog.dismiss();
                    }
                });

            }
        });
        enterNewPasswordDialog.show();
    }

    /**
     * Called when GoogleApiClient is connected
     *
     * @param connectionHint
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        googleSignInClicked = false;
        handleGoogleUser();
    }

    /**
     * Called when GoogleApiClient is suspended
     *
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * Handles Facebook sign in:
     * Creates an associated TipperUser account, if the Facebook user signs in for the
     * first time, and otherwise fetches the associated TipperUser, saves it as the global
     * variable and sends the user to MainActivity
     *
     * @param loginResult
     */
    public void handleFacebookUser(LoginResult loginResult) {
        final AccessToken accessToken = loginResult.getAccessToken();
        final TipperUser tUser = new TipperUser();
        GraphRequestAsyncTask request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject user, GraphResponse graphResponse) {
                final String email = user.optString("email");
                final String uuid = user.optString("id");
                final String name = user.optString("name");
                Log.d(ACTIVITY_ID, "Facebook login: Name: " + name + ", email: " + email + ", ID: " + uuid);

                // check if user already exist
                ParseQuery<TipperUser> query = ParseQuery.getQuery("TipperUser");
                query.whereEqualTo("uuid", uuid);
                query.getFirstInBackground(new GetCallback<TipperUser>() {
                    @Override
                    public void done(TipperUser tipperUser, ParseException e) {
                        if (e == null) {
                            if (tipperUser == null) {
                                try {
                                    if (LoginActivity.emailAlreadyExists(email)) {
                                        Toast.makeText(context, "Account already exists with this email.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // facebook user is new, create new TipperUser account
                                        Log.d(ACTIVITY_ID, "Created new facebook user");
                                        tUser.setEmail(email);
                                        tUser.setUuidString(uuid);
                                        tUser.setUsername(name.replaceAll("\\s", "") + uuid); // username is name concatenated with ID, as usernames have to be unique
                                        tUser.setFacebookUser(true);
                                        tUser.setGoogleUser(false);
                                        try {
                                            tUser.save();
                                        } catch (ParseException exception) {
                                            exception.printStackTrace();
                                        }
                                        ((Application) loginActivity.getApplication()).setCurrentUser(tUser);
                                        tUser.pinInBackground();
                                        Intent intent = new Intent(context, MainActivity.class);
                                        intent.putExtra("uuid", tUser.getUuidString());
                                        startActivity(intent);
                                    }
                                } catch (ParseException e1) {
                                    e1.printStackTrace();
                                }
                            } else {
                                // facebook user exist
                                Log.d(ACTIVITY_ID, "Facebook user exists");
                                ((Application) loginActivity.getApplication()).setCurrentUser(tipperUser);
                                tipperUser.pinInBackground();
                                Intent intent = new Intent(context, MainActivity.class);
                                intent.putExtra("uuid", uuid);
                                startActivity(intent);
                            }
                        } else {
                            Log.e(ACTIVITY_ID, "Parse error: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).executeAsync();
    }

    /**
     * Handles Google+ sign in:
     * Creates an associated TipperUser account, if the Google+ user signs in for the
     * first time, and otherwise fetches the associated TipperUser, saves it as the global
     * variable and sends the user to MainActivity
     */
    public void handleGoogleUser() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(googleApiClient) != null) {
                Person person = Plus.PeopleApi.getCurrentPerson(googleApiClient);
                final String name = person.getDisplayName();
                final String email = Plus.AccountApi.getAccountName(googleApiClient);
                final String uuid = person.getId();

                Log.d(ACTIVITY_ID, "Google+ login: Name: " + name + ", email: " + email + ", ID: " + uuid);

                // check if user already exist
                ParseQuery<TipperUser> query = ParseQuery.getQuery("TipperUser");
                query.whereEqualTo("uuid", uuid);
                query.getFirstInBackground(new GetCallback<TipperUser>() {
                    @Override
                    public void done(TipperUser tipperUser, ParseException e) {
                        if (tipperUser == null) {
                            try {
                                if (LoginActivity.emailAlreadyExists(email)) {
                                    Toast.makeText(context, "Account already exists with this email.", Toast.LENGTH_SHORT).show();
                                } else {
                                    // google user is new - creating a TipperUser account
                                    TipperUser user = new TipperUser();
                                    user.setUuidString(uuid);
                                    user.setEmail(email);
                                    user.setUsername(name.replaceAll("\\s", "") + uuid); // username is name concatenated with ID, as usernames have to be unique
                                    user.setGoogleUser(true);
                                    user.setFacebookUser(false);
                                    try {
                                        user.save();
                                    } catch (ParseException e1) {
                                        e1.printStackTrace();
                                    }
                                    ((Application) loginActivity.getApplication()).setCurrentUser(user);
                                    user.pinInBackground();
                                    Intent intent = new Intent(context, MainActivity.class);
                                    intent.putExtra("uuid", user.getUuidString());
                                    startActivity(intent);
                                }
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                            }
                        } else {
                            // google user is an existing TipperUser, save current user and continue
                            ((Application) loginActivity.getApplication()).setCurrentUser(tipperUser);
                            tipperUser.pinInBackground();
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.putExtra("uuid", uuid);
                            startActivity(intent);
                        }
                    }
                });
            } else {
                Log.e(ACTIVITY_ID, "Google Person is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Makes sure the GoogleApiClient is disconnected onStop of Activity
     */
    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }
}
