package kstr14.tipper;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import kstr14.tipper.Data.Group;
import kstr14.tipper.Data.TipperUser;

/**
 * Created by Kristine on 18-05-2015.
 */
public class ParseHelper {

    public static List<Group> getCurrentUsersGroups() throws ParseException {
        List<Group> groups = new ArrayList<>();
        boolean done = false;
        // fetch the current user's groups
        TipperUser user = (TipperUser) ParseUser.getCurrentUser();
        groups = user.getGroups().getQuery().find();
        System.out.println("ParseHelper fetched " + groups.size() + " groups");
        return groups;
    }

    public static Group getGroup(String name) throws ParseException {
        ParseQuery query = new ParseQuery("Group");
        List<Group> result = query.whereEqualTo("name", name).find();
        if(result.size() != 0) {
            return result.get(0);
        } else return null;
    }
}