package kstr14.tipper.Data;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.UUID;

/**
 * Created by Kristine on 14-05-2015.
 */

@ParseClassName("Group")
public class Group extends ParseObject {

    public void setName(String name) {
        put("name", name);
    }

    public String getName() {
        return getString("name");
    }

    public void setDescription(String description) {
        put("description", description);
    }

    public String getDescription() {
        return getString("description");
    }

    public void setClosed(boolean closed) {
        put("closed", closed);
    }

    public boolean isClosed() {
        return getBoolean("closed");
    }

    public void setCreator(ParseUser creator) {
        put("creator", creator);
    }

    public ParseUser getCreator() {
        return getParseUser("creator");
    }

    public ParseRelation<TipperUser> getUsers() {
        return getRelation("users");
    }

    public void addUser(TipperUser user) {
        getUsers().add(user);
        saveInBackground();
    }

    public void removeUser(TipperUser user) {
        getUsers().remove(user);
        saveInBackground();
    }

    public ParseRelation<Tip> getTips() {
        return getRelation("tips");
    }

    public void addTip(Tip tip) {
        getTips().add(tip);
        saveInBackground();
    }

    public void removeTip(Tip tip) {
        getTips().remove(tip);
        saveInBackground();
    }

    public void setUuidString() {
        UUID uuid = UUID.randomUUID();
        put("uuid", uuid.toString());
    }

    public String getUuidString() {
        return getString("uuid");
    }

}
