package kstr14.tipper.Data;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseRelation;

/**
 * Created by Kristine on 13-05-2015.
 */

@ParseClassName("Category")
public class Category extends ParseObject {

    public String getName() {
        return getString("name");
    }

    public void setName(String name) {
        put("name", name);
    }

    public ParseRelation<Tip> getTips() {
        return getRelation("tips");
    }

    public void addTip(Tip tip) {
        getTips().add(tip);
        saveInBackground();
    }
}
