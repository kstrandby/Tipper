package kstr14.tipper;

import com.parse.Parse;
import com.parse.ParseObject;

import kstr14.tipper.Data.Group;
import kstr14.tipper.Data.Tip;
import kstr14.tipper.Data.TipperUser;

/**
 * Class used for setting up Parse database and to hold global user object
 */
public class Application extends android.app.Application {

    private TipperUser currentUser; // global variable to hold the current user object

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Parse
        ParseObject.registerSubclass(Tip.class);
        ParseObject.registerSubclass(Group.class);
        ParseObject.registerSubclass(TipperUser.class);

        Parse.enableLocalDatastore(this); // for caching a user
        Parse.initialize(this, "arag2jjTvkvqlVVyv4jM41m1Y5uFLcMB3Thz4sAQ", "gPzHogMpGzBVfJmsd7hA8WMWKPcWlZy2py3bMH1x");
    }

    /**
     * Fetches the current user
     * @return the current user
     */
    public TipperUser getCurrentUser() {
        return currentUser;
    }

    /**
     * Sets the current user
     * @param user, the current user
     */
    public void setCurrentUser(TipperUser user) {
        currentUser = user;
    }

}