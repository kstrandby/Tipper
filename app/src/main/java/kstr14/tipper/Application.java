package kstr14.tipper;

import com.parse.Parse;
import com.parse.ParseObject;

import kstr14.tipper.Data.Category;
import kstr14.tipper.Data.Group;
import kstr14.tipper.Data.Tip;
import kstr14.tipper.Data.TipperUser;

/**
 * Created by Kristine on 14-05-2015.
 */

public class Application extends android.app.Application {

    private TipperUser currentUser;

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Parse
        ParseObject.registerSubclass(Tip.class);
        ParseObject.registerSubclass(Category.class);
        ParseObject.registerSubclass(Group.class);
        ParseObject.registerSubclass(TipperUser.class);

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "arag2jjTvkvqlVVyv4jM41m1Y5uFLcMB3Thz4sAQ", "gPzHogMpGzBVfJmsd7hA8WMWKPcWlZy2py3bMH1x");

        // initalize facebook
        //FacebookSdk.sdkInitialize(getApplicationContext());

    }

    public TipperUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(TipperUser user) {
        currentUser = user;
    }

}