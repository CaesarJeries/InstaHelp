package project.com.instahelp.utils;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import project.com.instahelp.R;

public class User implements Serializable{

    public final static int BRONZE_THRESHOLD = 5;
    public final static int SILVER_THRESHOLD = 10;
    public final static int GOLD_THRESHOLD = 15;
    public final static int PLATINUM_THRESHOLD = 20;

    public enum AccountType {Facebook, Google, Unspecified}

    public enum Rank{
        Unranked,
        Bronze,
        Silver,
        Gold,
        Platinum
    }

    private String id;
    private String first_name;
    private String last_name;
    private String email;
    private String phone_number;
    private AccountType account_type;
    private String profilePictureStringEncode;
    private int helpCount;


    public User(JSONObject u){
        account_type = AccountType.Facebook;
        phone_number = null;
        try{
            id = u.getString("id");
            String[] name_arr = u.getString("name").split(" ");
            first_name = name_arr[0];
            last_name = name_arr[1];
            email = u.getString("email");

        }catch(JSONException e){
            id = e.getMessage();
        }
        String url = "https://graph.facebook.com/" + id + "/picture?type=large";
        profilePictureStringEncode = getProfilePictureStringEncode(url);
        helpCount = 0;
    }

    public User(GoogleSignInAccount account){
        id = account.getId();
        String[] arr = account.getDisplayName().split(" ");
        first_name = arr[0];
        last_name = arr[1];
        email = account.getEmail();
        phone_number = null;
        account_type = AccountType.Google;
        String url = account.getPhotoUrl().toString();
        profilePictureStringEncode = getProfilePictureStringEncode(url);
        helpCount = 0;
    }

    public User(Map<String, String> map){
        id = map.get("id");
        first_name = map.get("firstName");
        last_name = map.get("lastName");
        account_type = AccountType.valueOf(map.get("accountType"));
        email = map.get("email");
        phone_number = map.get("phoneNumber");
        profilePictureStringEncode = map.get("imageEncoding");
        helpCount = Integer.valueOf(map.get("helpCount"));
    }


    public String getID() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return first_name;
    }

    public void setFirstName(String first_name) {
        this.first_name = first_name;
    }

    public String getLastName() {
        return last_name;
    }

    public void setLastName(String last_name) {
        this.last_name = last_name;
    }

    public String getName(){
        return first_name + " " + last_name;
    }

    public void setName(String newName){
        String[] arr = newName.split(" ");
        first_name = arr[0];
        last_name = null;
        if(arr.length > 1) {
            for(int i = 1; i< arr.length; i++){
                last_name += arr[i];
            }

        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phone_number;
    }

    public void setPhoneNumber(String phone_number) {
        this.phone_number = phone_number;
    }

    public AccountType getAccountType(){
        return account_type;
    }

    public void incrementHelpCount(){
        helpCount++;
    }

    public int getHelpCount(){
        return helpCount;
    }

    public Rank getRank(){
        if(helpCount >= PLATINUM_THRESHOLD) return Rank.Platinum;
        if(helpCount >= GOLD_THRESHOLD) return Rank.Gold;
        if(helpCount >= SILVER_THRESHOLD) return Rank.Silver;
        if(helpCount >= BRONZE_THRESHOLD) return Rank.Bronze;
        return Rank.Unranked;
    }

    private class DownloadImageTask extends AsyncTask<URL, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(URL... params) {
            Bitmap pic = null;
            URL picURL = params[0];
            try{
                pic = BitmapFactory.decodeStream(picURL.openConnection().getInputStream());
            }catch (IOException e){

            }
            return pic;
        }
    }


    private String getProfilePictureStringEncode(String url){
        Bitmap pic = null;
        URL picURL = null;
        try{
            picURL = new URL(url);
        } catch (MalformedURLException e){

        }

        try {
            pic = new DownloadImageTask().execute(picURL).get();
        }catch (ExecutionException | InterruptedException e){
            Log.e("DownloadPic", e.getMessage());
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        pic.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        pic.recycle();
        byte[] byteArray = outputStream.toByteArray();
        String retval = Base64.encodeToString(byteArray, Base64.DEFAULT);

        return retval;
    }

    public Map<String, String> toMap(){
        Map<String, String> retval = new HashMap<>();
        retval.put("id", id);
        retval.put("firstName", first_name);
        retval.put("lastName", last_name);
        retval.put("email", email);
        retval.put("phoneNumber", phone_number);
        retval.put("accountType", account_type.toString());
        retval.put("imageEncoding", profilePictureStringEncode);
        retval.put("helpCount", String.valueOf(helpCount));

        return retval;
    }


    public Bitmap getProfilePicture(){
        byte[] imageAsBytes = Base64.decode(profilePictureStringEncode, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }

    public void setProfilePicture(Bitmap image){
        profilePictureStringEncode = encodeImage(image);
    }

    public String getProfilePictureStringEncode(){
        return profilePictureStringEncode;
    }

    public static Bitmap decodePictureEncoding(String string){
        if(string == null) return null;
        byte[] imageAsBytes = Base64.decode(string, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }

    public static String encodeImage(Bitmap image){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public static Bitmap getRankImage(Rank rank, Resources res){
        Bitmap retval;
        switch(rank){
            case Unranked:{
                retval = BitmapFactory.decodeResource(res, R.drawable.unranked);
                break;
            }
            case Bronze:{
                retval = BitmapFactory.decodeResource(res, R.drawable.bronze);
                break;
            }
            case Silver:{
                retval = BitmapFactory.decodeResource(res, R.drawable.silver);
                break;
            }
            case Gold:{
                retval = BitmapFactory.decodeResource(res, R.drawable.gold);
                break;
            }
            case Platinum:{
                retval = BitmapFactory.decodeResource(res, R.drawable.platinum);
                break;
            }
            default:{
                retval = null;
            }
        }
        return retval;
    }

}
