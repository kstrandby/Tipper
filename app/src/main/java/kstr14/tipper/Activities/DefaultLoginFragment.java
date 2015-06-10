package kstr14.tipper.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.json.JSONObject;

import java.util.Arrays;

import kstr14.tipper.Application;
import kstr14.tipper.Data.TipperUser;
import kstr14.tipper.R;

public class DefaultLoginFragment extends Fragment implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String ACTIVITY_ID = "LoginActivity";

    public static final int GOOGLE_SIGN_IN = 1;

    private boolean googleSignInClicked;
    private boolean intentInProgress;

    private LoginButton facebookButton;
    private CallbackManager callbackManager;

    private Activity loginActivity;
    private Context context;

    private GoogleApiClient googleApiClient;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        loginActivity = getActivity();
        context = getActivity().getApplicationContext();
        FacebookSdk.sdkInitialize(context);
        callbackManager = CallbackManager.Factory.create();

        View view = inflater.inflate(R.layout.fragment_default_login, container, false);
        view.findViewById(R.id.google_sign_in_button).setOnClickListener(this);

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
                Log.e(ACTIVITY_ID, "Facebook login failed: " + e.getStackTrace().toString());
            }
        });
        // setup the google api
        googleApiClient = new GoogleApiClient.Builder(view.getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();

        facebookButton.setFragment(this);

        return view;

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!intentInProgress) {
            if (googleSignInClicked && result.hasResolution()) {
                // The user has already clicked 'sign-in' so we attempt to resolve all
                // errors until the user is signed in, or they cancel.
                try {
                    result.startResolutionForResult(loginActivity, GOOGLE_SIGN_IN);
                    intentInProgress = true;
                } catch (IntentSender.SendIntentException e) {
                    // The intent was canceled before it was sent.  Return to the default
                    // state and attempt to connect to get an updated ConnectionResult.
                    intentInProgress = false;
                    googleApiClient.connect();
                }
            }
        }
    }

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


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.google_sign_in_button && !googleApiClient.isConnecting()) {
            googleSignInClicked = true;
            googleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        googleSignInClicked = false;
        handleGoogleUser();
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

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
                        if (tipperUser == null) {
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

                        } else {
                            // facebook user exist
                            Log.d(ACTIVITY_ID, "Facebook user exists");
                            ((Application) loginActivity.getApplication()).setCurrentUser(tipperUser);
                            tipperUser.pinInBackground();
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.putExtra("uuid", uuid);
                            startActivity(intent);

                        }
                    }
                });
            }
        }).executeAsync();
    }

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
}
