package kstr14.tipper.Data;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseRelation;

import java.util.UUID;

/**
 * Created by Kristine on 17-05-2015.
 */

@ParseClassName("TipperUser")
public class TipperUser extends ParseObject {

    public void setUsername(String username) {
        put("username", username);
    }

    public String getUsername() {
        return getString("username");
    }

    public void setEmail(String email) {
        put("email", email);
    }

    public String getEmail() {
        return getString("email");
    }

    public void setPassword(String password) {
        put("password", password);
    }

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

    public void setUuidString() {
        UUID uuid = UUID.randomUUID();
        put("uuid", uuid.toString());
    }

    public String getUuidString() {
        return getString("uuid");
    }


}
