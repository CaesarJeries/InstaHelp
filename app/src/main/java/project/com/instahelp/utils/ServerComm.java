package project.com.instahelp.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import project.com.instahelp.activities.LoginActivity;
import project.com.instahelp.chat.ChatMessage;
import project.com.instahelp.utils.notifications.HelpOffer;
import project.com.instahelp.utils.notifications.Notification;
import project.com.instahelp.utils.notifications.Request;
import project.com.instahelp.utils.notifications.Response;

// firebase


public class ServerComm implements Firebase.AuthResultHandler{

    public static final String POSTS_CHILD = "Posts";
    private static final String USERS_CHILD = "Users";
    public static final String NOTIFICATIONS_CHILD = "Notifications";
    public static final String MESSAGES_CHILD = "Messages";


    private Context application_context = null;
    static private ServerComm instance;
    static private Firebase ref = null;

    private boolean auth_result = false;
    private FirebaseError auth_error = null;
    private SignInMode login_mode;
    private GoogleSignInResult googleSignInResult = null;
    private User current_user;

    public enum SignInMode {GOOGLE, FACEBOOK};

    private ServerComm(Context context){
        application_context = context;
    }


    static public void init(Context context){
        Firebase.setAndroidContext( context);
        ref = new Firebase(Defaults.FIREBASE_URL);
        instance = new ServerComm(context);
    }

    static public ServerComm getInstance() throws AppNotInitializedException{
        if(instance != null){
            return instance;
        }else{
            throw new AppNotInitializedException();
        }
    }

    @Override
    public void onAuthenticated(AuthData authData) {
        requestData(login_mode);
        auth_result = true;

    }
    @Override
    public void onAuthenticationError(FirebaseError firebaseError) {
        auth_error = firebaseError;
    }


    public void addGoogleSignInResult(GoogleSignInResult res){
        googleSignInResult = res;
    }

    public void authenticateSignIn(SignInMode mode, String authToken) {
        Firebase ref = new Firebase(Defaults.FIREBASE_URL);
        if(authToken != null){
            switch (mode) {
                case GOOGLE:
                    ref.authWithOAuthToken("google", authToken, this);
                    login_mode = SignInMode.GOOGLE;
                    break;
                case FACEBOOK:
                    ref.authWithOAuthToken("facebook", authToken, this);
                    login_mode = SignInMode.FACEBOOK;
                    break;
            }
        }else{
            /* Logged out of Facebook/Google so do a logout from the Firebase app */
            ref.unauth();
        }

    }

    public String getAuthErrorMessage(){
        if(auth_error == null){
            return "NoErrorMessage";
        }
        return auth_error.getMessage();
    }


    /**
     *  Post management methods
     */

    public void addPost(Post post){
        String id = ref.child(POSTS_CHILD).push().getKey();
        post.setID(id);
        ref.child(POSTS_CHILD).child(id).setValue(post.toMap());
    }

    public void addHelpOffer(final String post_id, final String user_id){
        ref.child(POSTS_CHILD).child(post_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                Post p = new Post(map);
                p.addHelpOffer(user_id);
                ref.child(POSTS_CHILD).child(post_id).setValue(p.toMap());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void removeHelpOffer(final String post_id, final String user_id){
        ref.child(POSTS_CHILD).child(post_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                Post p = new Post(map);
                p.removeHelpOffer(user_id);
                ref.child(POSTS_CHILD).child(post_id).setValue(p.toMap());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void deletePost(String post_id){
        ref.child(POSTS_CHILD).child(post_id).removeValue();
    }

    public void deleteRelatedNotifications(final String post_id, final String user_id){
        ref.child(NOTIFICATIONS_CHILD).child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    Map<String, String> map = (Map<String, String>) child.getValue();
                    Notification not = new Notification(map);
                    if(not.getPostID().equals(post_id)){
                        ref.child(NOTIFICATIONS_CHILD).child(user_id).child(not.getID()).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void setChosenUser(final String post_id, final String user_id){
        ref.child(POSTS_CHILD).child(post_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                Post p = new Post(map);
                p.setChosenUser(user_id);
                ref.child(POSTS_CHILD).child(post_id).setValue(p.toMap());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }


    public void addPermission(final String post_id, final String user_id, final Post.Permission perm){
        ref.child(POSTS_CHILD).child(post_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                Post p = new Post(map);
                p.addPermission(user_id, perm);
                ref.child(POSTS_CHILD).child(post_id).setValue(p.toMap());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }


    /**
     * Notification management methods
     */

    public void addNotification(Request request){
        String id = ref.child(NOTIFICATIONS_CHILD).push().getKey();
        request.setID(id);
        ref.child(NOTIFICATIONS_CHILD).child(request.getReceiverID()).child(id).setValue(request.toMap());
    }

    public void addNotification(Response response){
        String id = ref.child(NOTIFICATIONS_CHILD).push().getKey();
        response.setID(id);
        ref.child(NOTIFICATIONS_CHILD).child(response.getReceiverID()).child(id).setValue(response.toMap());
    }

    public void addNotification(HelpOffer helpOffer){
        String id = ref.child(NOTIFICATIONS_CHILD).push().getKey();
        helpOffer.setID(id);
        ref.child(NOTIFICATIONS_CHILD).child(helpOffer.getReceiverID()).child(id).setValue(helpOffer.toMap());
    }


    public void deleteNotification(String user_id, String id){
        ref.child(NOTIFICATIONS_CHILD).child(user_id).child(id).removeValue();
    }

    public void deleteNotification(final String sender_id, final String receiver_id, final String post_id){
        ref.child(NOTIFICATIONS_CHILD).child(receiver_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    Map<String, String> map = (Map<String, String>) child.getValue();
                    Notification not = new Notification(map);
                    if(not.getSenderID().equals(sender_id) && not.getPostID().equals(post_id)){
                        ref.child(NOTIFICATIONS_CHILD).child(receiver_id).child(not.getID()).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }





    /**
     *  User management methods
     */


    public void addUser(final User user){

        final String user_name = user.getID();

        ref.child(USERS_CHILD).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> map = (HashMap<String, Object>) dataSnapshot.child(user_name).getValue();
                if(map == null){
                    Firebase all_users = new Firebase(Defaults.FIREBASE_URL).child(USERS_CHILD);
                    Firebase user_ref = all_users.child(user_name);

                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(application_context);
                    String phoneNumber = pref.getString(Defaults.PreferencesTags.PHONE_NUMBER, null);
                    Map<String, String> fields = user.toMap();
                    if(phoneNumber != null){

                        // Case : user signed up with Google/facebook account, and now the user
                        // chose a different account (google or facebook account that they didn't use before).
                        fields.put("phoneNumber", phoneNumber);
                    }


                    user_ref.setValue(fields);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    
    public void updatePhoneNumber(final String user_name, final String phone_number){

        ref.child(USERS_CHILD).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = new User((HashMap<String, String>) dataSnapshot.child(user_name).getValue());
                user.setPhoneNumber(phone_number);
                Firebase all_users = new Firebase(Defaults.FIREBASE_URL).child(USERS_CHILD);
                Firebase user_ref = all_users.child(user_name);
                Map<String, String> map = user.toMap();
                user_ref.setValue(map);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    public void changeProfilePicture(final String user_id, final Bitmap picture){
        ref.child(USERS_CHILD).child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = new User((HashMap<String, String>) dataSnapshot.getValue());
                user.setProfilePicture(picture);
                Map<String, String> map = user.toMap();
                ref.child(USERS_CHILD).child(user_id).setValue(map);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void changeUserName(final String user_id, final String newName){
        ref.child(USERS_CHILD).child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = new User((HashMap<String, String>) dataSnapshot.getValue());
                user.setName(newName);
                Map<String, String> map = user.toMap();
                ref.child(USERS_CHILD).child(user_id).setValue(map);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }


    public void sendMessage(String receiver_id, ChatMessage message){
        ref.child(MESSAGES_CHILD).child(receiver_id).push().setValue(message.toMap());
    }

    public void deleteChat(String id){
        ref.child(MESSAGES_CHILD).child(id).removeValue();
    }

    public void incrementHelpCount(final String user_id){
        ref.child(USERS_CHILD).child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = new User((HashMap<String, String>) dataSnapshot.getValue());
                user.incrementHelpCount();
                Map<String, String> map = user.toMap();
                ref.child(USERS_CHILD).child(user_id).setValue(map);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void unauth(){
        Firebase ref = new Firebase(Defaults.FIREBASE_URL);
        ref.unauth();
    }

    // Exception classes

    public static class AppNotInitializedException extends Exception{
        @Override
        public String getMessage() {
            return "ServerHandler.init must be called before requesting an instance";
        }
    }

    public class UserNotFoundException extends Exception{
        private String userName;

        public UserNotFoundException(String userName){
            this.userName = userName;
        }

        @Override
        public String getMessage() {
            return "User " + userName + " is not found in the database.";
        }
    }


    private void requestData(SignInMode mode){
        switch(mode){
            case FACEBOOK:
                requestFacebookData();
                break;
            case GOOGLE:
                requestGoogleData();
                break;
        }
    }


    private void requestGoogleData(){
        GoogleSignInAccount acct = googleSignInResult.getSignInAccount();
        googleSignInResult = null;
        current_user = new User(acct);
        addUser(current_user);
        ((LoginActivity) application_context).onAuthenticated(current_user);
    }

    private void requestFacebookData() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject user, GraphResponse graphResponse) {
                        current_user = new User(user);
                        addUser(current_user);
                        ((LoginActivity) application_context).onAuthenticated(current_user);
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public Firebase getPostRef(){
        return ref.child(POSTS_CHILD);
    }
    public Firebase getUserRef(){
        return ref.child(USERS_CHILD);
    }
    public Firebase getNotificationsRef(String user_id){
        return ref.child(NOTIFICATIONS_CHILD).child(user_id);
    }
    public Firebase getMessagesRef(String id){
        return ref.child(MESSAGES_CHILD).child(id);
    }
}
