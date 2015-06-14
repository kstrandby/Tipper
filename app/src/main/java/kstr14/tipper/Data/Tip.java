package kstr14.tipper.Data;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.util.Date;
import java.util.UUID;

/**
 * Class for a tip object
 * Only contains getters and setters for fetching and updating attributes of the object in the
 * database
 */
@ParseClassName("Tip")
public class Tip extends ParseObject {

    // default constructor
    public Tip(){ }

    public String getTitle() {
        return getString("title");
    }

    public String getLowerCaseTitle() {
        return getString("lowerCaseTitle");
    }

    public void setTitle(String title) {
        put("title", title);
        put("lowerCaseTitle", title.toLowerCase());
    }

    public String getDescription() {
        return getString("description");
    }

    public void setDescription(String description) {
        put("description", description);
    }

    public int getPrice() {
        return getNumber("price").intValue();
    }

    public void setPrice(int price) {
        put("price", price);
    }

    public int getUpvotes() {
        return getNumber("upvotes").intValue();
    }

    public void setUpvotes(int upvotes) {
        put("upvotes", upvotes);
    }

    public int getDownvotes() {
        return getNumber("downvotes").intValue();
    }

    public void setDownvotes(int downvotes) {
        put("downvotes", downvotes);
    }

    public Date getStartDate() {
        return getDate("startDate");
    }

    public void setStartDate(Date startDate) {
        put("startDate", startDate);
    }

    public Date getEndDate() {
        return getDate("endDate");
    }

    public void setEndDate(Date endDate) {
        put("endDate", endDate);
    }

    public void setPrivate(boolean value) {
        put("private", value);
    }

    public boolean isPrivate() {
        return getBoolean("private");
    }

    public void setUuidString() {
        UUID uuid = UUID.randomUUID();
        put("uuid", uuid.toString());
    }

    public String getUuidString() {
        return getString("uuid");
    }

    public String getCategory() {
        return getString("category");
    }

    public void setCategory(String category) {
        put("category", category);
    }

    public void setGroup(ParseObject group) {
        put("group", group);
    }

    public ParseObject getGroup() {
        return getParseObject("group");
    }

    public ParseFile getImage() {
        return getParseFile("image");
    }

    public void setImage(ParseFile file) {
        put("image", file);
    }

    public ParseObject getCreator() {
        return getParseObject("creator");
    }

    public void setCreator(ParseObject user) {
        put("creator", user);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public void setLocation(ParseGeoPoint location) {
        put("location", location);
    }

    public Tip copy() {
        Tip tip = new Tip();
        tip.setTitle(this.getTitle());
        tip.setDescription(this.getDescription());
        tip.setCategory(this.getCategory());
        tip.setPrice(this.getPrice());
        tip.setUuidString();
        tip.setStartDate(this.getStartDate());
        tip.setEndDate(this.getEndDate());
        tip.setCreator(this.getCreator());
        tip.setDownvotes(this.getDownvotes());
        tip.setUpvotes(this.getUpvotes());
        if(this.getGroup() != null) {
            tip.setGroup(this.getGroup());
        }
        if(this.getImage() != null) {
            tip.setImage(this.getImage());
        }
        if(this.getLocation() != null) {
            tip.setLocation(this.getLocation());
        }
        tip.setPrivate(this.isPrivate());
        return tip;
    }
}
