package kstr14.tipper.Data;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseRelation;

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

    public void setTitle(String title) {
        put("title", title);
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
        return getDate("EndDate");
    }

    public void setEndDate(Date endDate) {
        put("endDate", endDate);
    }


    public void setUuidString() {
        UUID uuid = UUID.randomUUID();
        put("uuid", uuid.toString());
    }

    public String getUuidString() {
        return getString("uuid");
    }

    public ParseRelation<Category> getCategoriesRelation() {
        return getRelation("categories");
    }

    public void addCategory(Category category) {
        getCategoriesRelation().add(category);
        saveInBackground();
    }

    public void removeCategory(Category category) {
        getCategoriesRelation().remove(category);
        saveInBackground();
    }

    public ParseFile getImage() {
        return getParseFile("image");
    }

    public void setImage(ParseFile file) {
        put("image", file);
    }
}
