package kstr14.tipper;

import com.parse.ParseException;

import java.util.List;

import kstr14.tipper.Data.Group;
import kstr14.tipper.Data.Tip;
import kstr14.tipper.Data.TipperUser;

/**
 * Created by Kristine on 18-05-2015.
 */
public class ParseHelper {

    public ParseHelper(){}


    public static List<Group> getUsersGroups(TipperUser user) throws ParseException {
        List<Group> groups = user.getGroups().getQuery().find();
        System.out.println("ParseHelper fetched " + groups.size() + " groups");
        return groups;
    }

    public static List<Tip> getTipsOfGroup(Group group) throws ParseException {
        List<Tip> tips = group.getTips().getQuery().find();
        System.out.println("ParseHelpher fetched " + tips.size() + " tips of group " + group.getName());
        return tips;
    }
}
