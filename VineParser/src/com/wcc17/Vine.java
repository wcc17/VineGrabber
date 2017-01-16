package com.wcc17;

/**
 * Created by wcc17 on 1/15/17.
 */
public class Vine {

    int index;
    String avatarUrl = new String();
    String created = new String();
    String description = new String();
    String likes = new String();
    String loops = new String();
    String username = new String();
    String venueAddress = new String();
    String venueCity = new String();
    String venueCountryCode = new String();
    String venueName = new String();
    String venueState = new String();
    String videoUrl = new String();

    @Override
    public String toString() {
        return index + " - " + username.replace("username: ", "") + " - " + created.replace("created: ", "");
    }
}
