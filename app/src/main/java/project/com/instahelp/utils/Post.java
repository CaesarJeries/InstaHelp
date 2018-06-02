package project.com.instahelp.utils;

import android.location.Location;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import project.com.instahelp.fragments.DatePickerFragment;
import project.com.instahelp.fragments.TimePickerFragment;

public class Post implements Serializable {

    public enum Permission{
        PhoneNumber,
        Location;
    }

    // Post attributes
    private String postId;
    private String userId;

    private String title;
    private String userName;
    private String content;
    private String phoneNumber;
    private Map<String, String> location;
    private boolean locationPrivate;
    private boolean phoneNumberPrivate;
    private String date;
    private String time;
    private Status status;
    private Map<String, String> helpOffers;
    private String chosenUserId;

    /**
     * maps a user_id to a permission type : phone number or location.
     * holds a map of permission that the post owner gave other users.
     */
    private Map<String, String> permissions;

    private boolean stringToBoolean(String s){
        return s.compareTo("1") == 0;
    }
    private String booleanToString(boolean b){
        if(b){
            return "1";
        }
        return "0";
    }


    public enum Status{
        Assigned,
        NotAssigned
    }

    // convert database result to Post
    public Post(Map<String, Object> map){
        postId = (String) map.get("postId");
        userId = (String) map.get("userId");
        title = (String) map.get("title");
        userName = (String) map.get("userName");
        content = (String) map.get("content");
        phoneNumber = (String) map.get("phoneNumber");
        location = (Map<String, String>) map.get("location");
        locationPrivate = stringToBoolean((String) map.get("locationPrivacy"));
        phoneNumberPrivate = stringToBoolean((String) map.get("phonePrivacy"));
        helpOffers = (Map<String, String>) map.get("helpOffers");
        if(helpOffers == null) helpOffers = new HashMap<>();
        status = Status.valueOf((String) map.get("status"));
        chosenUserId = (String) map.get("chosenUserId");

        permissions = (Map<String, String>) map.get("permissions");
        if(permissions == null) permissions = new HashMap<>();
        time = (String) map.get("time");
        date = (String) map.get("date");
    }


    // Called from AddPost activity.

    public Post(String userId, String title, String userName, String content,
                String phoneNumber,
                boolean locationPrivate, boolean phoneNumberPrivate,
                String date, String time
    ) {
        this.userId = userId;
        this.title = title;
        this.userName = userName;
        this.content = content;
        this.phoneNumber = phoneNumber;
        this.locationPrivate = locationPrivate;
        this.phoneNumberPrivate = phoneNumberPrivate;
        this.date = date;
        this.time = time;
        this.status = Status.NotAssigned;
        this.helpOffers = new HashMap<>();
        location = new HashMap<>();
        permissions = new HashMap<>();
    }



    public String getID() {
        return postId;
    }
    public void setID(String id){
        postId = id;
    }

    public String getTitle() {
        return title;
    }

    public String getUserName() {
        return userName;
    }

    public String getContent() {
        return content;
    }

    public String getUserId() {
        return userId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Map<String, String> getLocation() {
        return location;
    }

    public boolean isLocationPrivate() {
        return locationPrivate;
    }

    public String getDate(){
        return date;
    }

    public String getTime(){
        return time;
    }

    public Status getStatus(){
        return status;
    }

    public boolean isPhoneNumberPrivate() {
        return phoneNumberPrivate;
    }

    public void setLocation(Location loc){
        location.put("lat", String.valueOf(loc.getLatitude()));
        location.put("long", String.valueOf(loc.getLongitude()));
    }

    /**
     * Help offers management methods
     *
     */

    public Map<String, String> getHelpOffers(){
        return helpOffers;
    }

    public void addHelpOffer(String user_id){
        if(helpOffers == null){
            helpOffers = new HashMap<>();
        }
        if(helpOffers.containsKey(user_id) == false){
            helpOffers.put(user_id, user_id);
        }
    }

    public void removeHelpOffer(String user_id){
        helpOffers.remove(user_id);
    }

    public boolean userOfferedHelp(String user_id){
        return helpOffers.containsKey(user_id);
    }

    public void setChosenUser(String user_id){
        if(helpOffers != null) helpOffers = null;
        chosenUserId = user_id;
        status = Status.Assigned;
    }

    public String getChosenUserId(){
        return chosenUserId;
    }


    /**
     *  Permissions management methods
     */

    public void addPermission(String user_id, Permission permission){
        permissions.put(user_id, permission.toString());
    }

    public boolean hasPermission(String user_id, Permission permission){
        String p = permissions.get(user_id);
        if(p != null){
            String str1 = p.toString();
            String str2 = permission.toString();
            return p.toString().compareTo(permission.toString()) == 0;
        }

        return false;
    }


    public Map<String, Object> toMap(){
        Map<String, Object> retval = new TreeMap<>();
        retval.put("postId", postId);
        retval.put("userId", userId);
        retval.put("userName", userName);
        retval.put("title", title);
        retval.put("content", content);
        retval.put("phoneNumber", phoneNumber);
        retval.put("location", location);
        retval.put("locationPrivacy", booleanToString(locationPrivate));
        retval.put("phonePrivacy", booleanToString(phoneNumberPrivate));
        retval.put("time", time);
        retval.put("date", date);
        retval.put("status", status.toString());
        retval.put("helpOffers", helpOffers);
        retval.put("chosenUserId", chosenUserId);
        retval.put("permissions", permissions);

        return retval;
    }


    static public String parseTime(TimePickerFragment fragment){
        return fragment.getTime();
    }

    static public String parseDate(DatePickerFragment fragment){
        return fragment.getDate();
    }
}
