package kstr14.tipper.Data;

import com.parse.ParseRelation;
import com.parse.ParseUser;

/**
 * Created by Kristine on 17-05-2015.
 */
public class TipperUser extends ParseUser {

    public ParseRelation<Group> getGroups() {
        return getRelation("groups");
    }

    public void addGroup(Group group) {
        getGroups().add(group);
        saveInBackground();
    }

    public void removeGroup(Group group) {
        getGroups().remove(group);
        saveInBackground();
    }


}
