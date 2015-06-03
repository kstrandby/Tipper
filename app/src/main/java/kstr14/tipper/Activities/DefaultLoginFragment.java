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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import kstr14.tipper.Application;
import kstr14.tipper.Data.TipperUser;
import kstr14.tipper.R;

public class DefaultLoginFragment extends Fragment implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "DefaultLoginFragment";

    public static final int GOOGLE_SIGN_IN = 1;

    private boolean googleSignInClicked;
    private boolean intentInProgress;
    private ConnectionResult connectionResult;

    private Activity loginActivity;
    private Context context;

    private GoogleApiClient googleApiClient;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_default_login, container, false);
        view.findViewById(R.id.google_sign_in_button).setOnClickListener(this);

        // setup the google api
        googleApiClient = new GoogleApiClient.Builder(view.getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();

        loginActivity = getActivity();
        context = getActivity().getApplicationContext();
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

    public void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == GOOGLE_SIGN_IN) {
            // reset the flags
            if (responseCode != Activity.RESULT_OK) {
                googleSignInClicked = false;
            }
            intentInProgress = false;
            if (!googleApiClient.isConnected()) {
                googleApiClient.reconnect();
            }
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

    public void handleGoogleUser() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(googleApiClient) != null) {
                Person person = Plus.PeopleApi.getCurrentPerson(googleApiClient);
                final String name = person.getDisplayName();
                final String email = Plus.AccountApi.getAccountName(googleApiClient);
                final String uuid = person.getId();
                Log.d(TAG, "Google+ login: Name: " + name + ", plusProfile: "
                         + ", email: " + email + ", ID: " + uuid);

                // check if user already exist
                ParseQuery<TipperUser> query = ParseQuery.getQuery("TipperUser");
                query.whereEqualTo("uuid", uuid);
                query.getFirstInBackground(new GetCallback<TipperUser>() {
                    @Override
                    public void done(TipperUser tipperUser, ParseException e) {
                        if(tipperUser == null) {
                            // google user is new - creating a TipperUser account
                            TipperUser user = new TipperUser();
                            user.setUuidString(uuid);
                            user.setEmail(email);
                            user.setUsername(name.replaceAll("\\s","") + uuid); // username is name concatenated with ID, as usernames have to be unique
                            user.setGoogleUser();
                            try {
                                user.save();
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                            }
                            ((Application)loginActivity.getApplication()).setCurrentUser(user);
                            user.pinInBackground();
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.putExtra("uuid", user.getUuidString());
                            startActivity(intent);
                        } else {
                            // google user is an existing TipperUser, save current user and continue
                            ((Application)loginActivity.getApplication()).setCurrentUser(tipperUser);
                            tipperUser.pinInBackground();
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.putExtra("uuid", uuid);
                            startActivity(intent);
                        }

                    }
                });

            } else {
                Log.e(TAG, "Google Person is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
