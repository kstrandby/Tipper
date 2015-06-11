package kstr14.tipper.Data;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Kristine on 09-05-2015.
 */

/**
 * Field values in Tip:
 *  - Title (String)
 *  - Description (String)
 *  - Price (int)
 *  - Upvotes (int)
 *  - Downvotes (int)
 *  - StartDate (Date)
 *  - EndDate (Date)
 *  - Categories (List of Category (enum))
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

}
