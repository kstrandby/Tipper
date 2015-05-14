package kstr14.tipper;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;

/**
 * Created by Kristine on 14-05-2015.
 */

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "arag2jjTvkvqlVVyv4jM41m1Y5uFLcMB3Thz4sAQ", "gPzHogMpGzBVfJmsd7hA8WMWKPcWlZy2py3bMH1x");
        ParseFacebookUtils.initialize(getApplicationContext());

    }

}