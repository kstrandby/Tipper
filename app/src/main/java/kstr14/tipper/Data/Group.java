package kstr14.tipper.Data;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseRelation;

import java.util.UUID;

/**
 * Class for a group object
 * Only contains getters and setters for fetching and updating attributes of the object in the
 * database
 */

@ParseClassName("Group")
public class Group extends ParseObject {

    public void setName(String name) {
        put("name", name);
        put("lowerCaseName", name.toLowerCase());
    }

    public String getName() {
        return getString("name");
    }

    public String getLowerCaseName() {
        return getString("lowerCaseName");
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

    public void setCreator(TipperUser creator) {
        put("creator", creator);
    }

    public ParseObject getCreator() throws ParseException {
        return fetchIfNeeded().getParseObject("creator");
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

    public void setImage(ParseFile image) {
        put("image", image);
    }

    public ParseFile getImage() {
        return getParseFile("image");
    }
}
