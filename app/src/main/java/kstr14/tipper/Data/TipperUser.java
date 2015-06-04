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

    public String getPassword() {
        return getString("password");
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

    public UUID setUuidString() {
        UUID uuid = UUID.randomUUID();
        put("uuid", uuid.toString());
        return uuid;
    }

    /**
     * method for creating TipperUser using their Google+ ID
     * as the uuid
     * @param uuid
     */
    public void setUuidString(String uuid) {
        put("uuid", uuid.toString());
    }

    public ParseRelation<Tip> getFavourites() {
        return getRelation("favourites");
    }

    public void addFavourite(Tip tip){
        getFavourites().add(tip);
        saveInBackground();
    }

    public void removeFavourite(Tip tip) {
        getFavourites().remove(tip);
        saveInBackground();
    }

    public String getUuidString() {
        return getString("uuid");
    }

    public void setGoogleUser() {
        put("google", true);
    }

    public boolean isGoogleUser() {
        return getBoolean("google");
    }

    @Override
    public boolean equals(Object other){
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof TipperUser))return false;
        TipperUser otherUser = (TipperUser)other;
        if(this.getUuidString().equals(otherUser.getUuidString())) return true;
        else return false;
    }
}
