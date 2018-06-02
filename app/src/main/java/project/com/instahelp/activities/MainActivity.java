package project.com.instahelp.activities;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import project.com.instahelp.R;
import project.com.instahelp.adapters.MyRequestsAdapter;
import project.com.instahelp.adapters.NotificationsAdapter;
import project.com.instahelp.adapters.PendingRequestsAdapter;
import project.com.instahelp.adapters.UserFeedAdapter;
import project.com.instahelp.adapters.ViewPagerAdapter;
import project.com.instahelp.chat.Chat;
import project.com.instahelp.chat.ChatActivity;
import project.com.instahelp.chat.ChatListAdapter;
import project.com.instahelp.chat.ChatListFragment;
import project.com.instahelp.chat.ChatMessage;
import project.com.instahelp.fragments.NotificationsFragment;
import project.com.instahelp.fragments.RequestsFragment;
import project.com.instahelp.fragments.UserFeedFragment;
import project.com.instahelp.fragments.UserProfileFragment;
import project.com.instahelp.interfaces.BadgeManager;
import project.com.instahelp.interfaces.DataManager;
import project.com.instahelp.interfaces.DisplayManager;
import project.com.instahelp.interfaces.LocalDataManager;
import project.com.instahelp.interfaces.LocationManager;
import project.com.instahelp.interfaces.RefManager;
import project.com.instahelp.services.LocationService;
import project.com.instahelp.services.NotificationService;
import project.com.instahelp.utils.Post;
import project.com.instahelp.utils.ServerComm;
import project.com.instahelp.utils.User;
import project.com.instahelp.utils.notifications.Notification;

public class MainActivity extends AppCompatActivity implements
        LocationManager, RefManager{

    final static int REQUEST_PERMISSIONS_CODE = 23;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    int currentItem;

    private static ServerComm comm = null;

    private UserFeedFragment userFeedFragment;
    private UserProfileFragment userProfileFragment;
    private RequestsFragment requestsFragment;
    private NotificationsFragment notificationsFragment;
    private ChatListFragment chatListFragment;

    private static Location mCurrentLocation;

    private static ServerDataSync dataSync;
    private DisplayManager displayManager;
    static private DataManager dataManager;
    static private ChatListManager chatListManager;
    static private BadgeManager badgeManager;
    static private LocalDataManager localDataManager;

    private static Context context;


    private int[] tabIcons = {
            R.mipmap.ic_tab_profile,
            R.mipmap.ic_tab_user_feed,
            R.mipmap.ic_tab_requests,
            R.mipmap.ic_tab_notifications,
            R.mipmap.ic_tab_chat
    };

    private enum AdapterType{
        POST_FEED,
        MY_REQUESTS,
        PENDING_REQUESTS,
        NOTIFICATIONS,
        CHAT
    }

    BroadcastReceiver receiver;

    public static DataManager getDataManager(){
        return dataManager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        initComm();
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        requestRuntimePermissions();
        localDataManager = new LocalDataManagerImpl();
        startServices();
        dataManager = new DataManagerImpl();
        chatListManager = new ChatListManager();
        dataSync = new ServerDataSync(this);
        displayManager = new DisplayManagerImpl();
        badgeManager = new BadgeManagerImpl(this);

        // Setup ViewPager
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        setUpTabLayout();
    }

    private void startServices(){
        Intent intent = new Intent(this, NotificationService.class);
        intent.putExtra("user_id", localDataManager.getCurrentUser().getID());
        startService(intent);
    }

    public static ServerComm getServerComm(){
        return comm;
    }


    public static LocalDataManager getLocalDataManager(){
        return localDataManager;
    }

    public class LocalDataManagerImpl implements LocalDataManager{
        private User user;
        @Override
        public User getCurrentUser() {
            if(user == null){
                SharedPreferences pref = getSharedPreferences("current_user", MODE_PRIVATE);
                String currentID = pref.getString("current_id", null);
                Gson gson = new Gson();
                String src = pref.getString(currentID, null);
                this.user = gson.fromJson(src, User.class);
            }
            return user;
        }

        @Override
        public void updateUser(User user) {

            SharedPreferences pref = getSharedPreferences("current_user", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            String currentID = pref.getString("current_id", null);
            Gson gson = new Gson();
            User currentUser = gson.fromJson(pref.getString(currentID, null), User.class);
            if(currentUser != null){
                if(currentUser.getPhoneNumber() != null){
                    user.setPhoneNumber(currentUser.getPhoneNumber());
                }
            }

            boolean firstLogin = pref.getString("firstLogin", null).compareTo("true") == 0;
            // check if user completed a request.
            if(!firstLogin){
                if(user.getHelpCount() > currentUser.getHelpCount()){
                    displayAchievementNotification(currentUser.getRank(), user);
                }
            }else{
                editor.putString("firstLogin", "false");
            }

            this.user = user;
            String json = gson.toJson(user);
            editor.putString(user.getID(), json);
            editor.apply();
            if(userProfileFragment != null){
                userProfileFragment.refresh();
            }

        }

        @Override
        public void updateUserName(String newName) {
            SharedPreferences pref = getSharedPreferences("current_user", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            Gson gson = new Gson();
            User currentUser = gson.fromJson(pref.getString("current_user", null), User.class);
            currentUser.setName(newName);
            String json = gson.toJson(user);
            editor.putString("current_user", json);
            editor.apply();

        }

        @Override
        public void updatePhoneNumber(String newNumber) {
            SharedPreferences pref = getSharedPreferences("current_user", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            Gson gson = new Gson();
            User currentUser = gson.fromJson(pref.getString("current_user", null), User.class);
            currentUser.setPhoneNumber(newNumber);
            String json = gson.toJson(user);
            editor.putString("current_user", json);
            editor.apply();
        }

        private void displayAchievementNotification(User.Rank previousRank, User user){
            if(user.getRank().ordinal() > previousRank.ordinal() ){
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                builder.setSmallIcon(R.mipmap.ic_achievement);
                builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_achievement));
                builder.setContentTitle("InstaHelp");
                builder.setContentText("Achievement unlocked");
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(1, builder.build());
            }
        }
    }

    public class BadgeManagerImpl implements BadgeManager {

        private Context context;
        public BadgeManagerImpl(Context context){
            this.context = context;
        }
        @Override
        public void increment(Tab tab) {
            View view = tabLayout.getTabAt(tab.toInt()).getCustomView();
            TextView badgeView = (TextView) view.findViewById(R.id.badge);
            String str = badgeView.getText().toString();
            int count = 0;
            if(str.isEmpty() == false) {
                count = Integer.valueOf(str);
            }
            count++;
            badgeView.setText(String.valueOf(count));
            badgeView.invalidate();
            if(badgeView.getVisibility() == View.GONE){
                badgeView.setVisibility(View.VISIBLE);
                playNotificationSound();
            }
        }

        @Override
        public void hide(Tab tab) {
            View view = tabLayout.getTabAt(tab.toInt()).getCustomView();
            TextView badgeView = (TextView) view.findViewById(R.id.badge);
            badgeView.setText("");
            badgeView.setVisibility(View.GONE);
            badgeView.invalidate();
        }

        private void playNotificationSound(){
            MediaPlayer mPlayer = MediaPlayer.create(context, R.raw.notification_tone);
            mPlayer.start();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });

        }
    }

    public static BadgeManager getBadgeManager(){
        return badgeManager;
    }

    private void setUpTabLayout(){
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        for(int i = 0 ; i < 3 ; i++){ // set icons for user profile/ user feed/ requests fragments.
            tabLayout.getTabAt(i).setIcon(tabIcons[i]);
        }

        String notificationsFragLabel = getResources().getString(R.string.notifications_fragment_label);
        String chatFragLabel = getResources().getString(R.string.chat_fragment_label);
        // set badge icons for notifications tab
        TabLayout.Tab tab = tabLayout.getTabAt(3);
        tab.setCustomView(R.layout.tab_badge_icon);
        ImageView iconView = (ImageView) tab.getCustomView().findViewById(R.id.icon);
        iconView.setImageResource(R.mipmap.ic_tab_notifications);
        TextView badgeView = (TextView) tab.getCustomView().findViewById(R.id.badge);
        badgeView.setText("");
        TextView titleView = (TextView) tab.getCustomView().findViewById(R.id.title);
        titleView.setText(notificationsFragLabel);

        // set badge icons for chat tab
        tab = tabLayout.getTabAt(4);
        tab.setCustomView(R.layout.tab_badge_icon);
        iconView = (ImageView) tab.getCustomView().findViewById(R.id.icon);
        iconView.setImageResource(R.mipmap.ic_tab_chat);
        badgeView = (TextView) tab.getCustomView().findViewById(R.id.badge);
        badgeView.setText("");
        titleView = (TextView) tab.getCustomView().findViewById(R.id.title);
        titleView.setText(chatFragLabel);

    }


    private void requestRuntimePermissions(){
        int numAdded = 0;
        int permissionCheck;
        String[] permissions = new String[2];
        // implicitly includes COARSE_LOCATION
        permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionCheck == PackageManager.PERMISSION_DENIED){
            permissions[0] = Manifest.permission.ACCESS_FINE_LOCATION;
            numAdded++;
        }
        // Implicitly includes read privileges.
        permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck == PackageManager.PERMISSION_DENIED){
            permissions[1] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            numAdded++;
        }

        if(numAdded > 0){
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS_CODE);
        }else{
            setUpLocationService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    setUpLocationService();

                } else {

                    Toast.makeText(MainActivity.this, "Well, shit!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == UserFeedFragment.ADD_POST_REQ){
            if(resultCode == Activity.RESULT_OK){
                Post post = (Post) data.getSerializableExtra("post");
                asyncAddPost(post);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.actionBar_logout:
                showLogOutConfirmation();
                return true;
        }
        return false;
    }



    @Override
    public void onDestroy(){
        if(receiver != null){
            unregisterReceiver(receiver);
        }
        super.onDestroy();
        dataSync.cleanup();
        stopService(new Intent(this, LocationService.class));
    }


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        initFragments();

        String profileFragLabel = getResources().getString(R.string.profile_fragment_label);
        String feedFragLabel = getResources().getString(R.string.feed_fragment_label);
        String requestsFragLabel = getResources().getString(R.string.requests_fragment_label);
        String notificationsFragLabel = getResources().getString(R.string.notifications_fragment_label);
        String chatFragLabel = getResources().getString(R.string.chat_fragment_label);

        adapter.addFragment(userProfileFragment, profileFragLabel);
        adapter.addFragment(userFeedFragment, feedFragLabel);
        adapter.addFragment(requestsFragment, requestsFragLabel);
        adapter.addFragment(notificationsFragment, notificationsFragLabel);
        adapter.addFragment(new ChatListFragment(), chatFragLabel);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1);
        viewPager.setOffscreenPageLimit(4);
        currentItem = 1;
    }

    @Override
    public Query getPostRef() {
        return comm.getPostRef();
    }

    @Override
    public Query getUserRef() {
        return comm.getUserRef();
    }


    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onPause();
        currentItem = viewPager.getCurrentItem();
    }

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
                new IntentFilter(LocationService.BROADCAST_ACTION));
        super.onResume();
        viewPager.setCurrentItem(currentItem);
    }

    private void showLogOutConfirmation(){
        String title = getResources().getString(R.string.log_out_alert_title);
        String content = getResources().getString(R.string.log_out_alert_content);
        String label = getResources().getString(R.string.log_out_alert_button_label);

        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton(label, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        signOut();
                    }

                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    void signOut() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("logout", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK); // To clean up all activities

        startActivity(intent);
        finish();
    }

    private void initComm(){
        try{
            comm = ServerComm.getInstance();
        }catch (ServerComm.AppNotInitializedException e){
            Log.e("Firebase", e.getMessage());
        }
    }

    private class AddLocationTask extends AsyncTask<Void, Void, Void>{
        private Post post;

        public AddLocationTask(Post p){
            this.post = p;
        }
        @Override
        protected Void doInBackground(Void... params) {

            boolean eq1 = true;
            boolean eq2 = true;
            while(eq1 && eq2){
                Location loc = getCurrentLocation();
                if(loc != null){
                    Double locationLong = loc.getLatitude();
                    Double locationLat = loc.getLatitude();
                    eq1 = locationLong.compareTo(0.0) == 0;
                    eq2 = locationLat.compareTo(0.0) == 0;
                }
            }

            post.setLocation(mCurrentLocation);
            // finally.
            comm.addPost(post);

            return null;
        }
    }

    public static synchronized void onReceiveLocation(Location location)  {
        mCurrentLocation = location;
        if(dataSync != null){
            dataSync.notifyAllAdapters();
        }
    }

    public static void onProviderDisabled(){
        showGPSAlert();
    }


    private void asyncAddPost(Post post){
        new AddLocationTask(post).execute();
    }


    @Override
    public synchronized Location getCurrentLocation(){
        return mCurrentLocation;
    }

    private void setUpLocationService(){
        Intent intent = new Intent(this, LocationService.class);
        startService(intent);
    }


    private static void showGPSAlert(){

        String gpsAlertTitle = context.getResources().getString(R.string.gps_alert_title);
        String gpsAlertContent = context.getResources().getString(R.string.gps_alert_content);
        new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(gpsAlertTitle)
                .setMessage(gpsAlertContent)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                })
                .show();
    }

    private void initFragments(){
        // UserFeedFragment
        userFeedFragment = new UserFeedFragment();
        userFeedFragment.setAdapter((UserFeedAdapter) dataSync.getAdapter(AdapterType.POST_FEED));
        userFeedFragment.setDisplayManager(displayManager);

        // RequestsFragment
        requestsFragment = new RequestsFragment();
        requestsFragment.setDisplayManager(displayManager);
        requestsFragment.setAdapter(RequestsFragment.Position.MY_REQUESTS, dataSync.getAdapter(AdapterType.MY_REQUESTS));
        requestsFragment.setAdapter(RequestsFragment.Position.PENDING_REQUESTS, dataSync.getAdapter(AdapterType.PENDING_REQUESTS));

        // NotificationsFragment
        notificationsFragment = new NotificationsFragment();
        notificationsFragment.setAdapter(dataSync.getAdapter(AdapterType.NOTIFICATIONS));

        // ChatListFragment
        chatListFragment = new ChatListFragment();
        chatListFragment.setAdapter(dataSync.getAdapter(AdapterType.CHAT));
        // UserProfileFragment
        userProfileFragment = new UserProfileFragment();
    }

    private class ServerDataSync{

        Activity activity;
        private Query mPostRef;
        private Query mUserRef;
        private Query mNotificationsRef;
        private Map<String, Post> mPosts;
        private Map<String, User> mUsers;
        private Map<String, Notification> mNotifications;

        private ChildEventListener mPostListener;
        private ChildEventListener mUserListener;
        private ChildEventListener mNotificationsListener;

        private Map<AdapterType, BaseAdapter> adapterMap;

        public ServerDataSync(Activity activity){
            this.activity = activity;
            mPosts = new HashMap<>();
            mUsers = new HashMap<>();
            mNotifications = new HashMap<>();
            mPostRef = comm.getPostRef();
            mUserRef = comm.getUserRef();
            final String user_id = localDataManager.getCurrentUser().getID();
            mNotificationsRef = comm.getNotificationsRef(user_id);

            mPostListener = this.mPostRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    Post p = new Post(map);
                    mPosts.put(p.getID(), p);
                    notifyAllAdapters();

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    Post p = new Post(map);
                    mPosts.put(p.getID(), p);
                    notifyAllAdapters();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    Post p = new Post(map);
                    mPosts.remove(p.getID());
                    notifyAllAdapters();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    // do nothing.
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.e("UserFeedAdapter", "Post listener was cancelled.");
                }

            });
            mUserListener = this.mUserRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                    Map<String, String> map = (Map<String, String>) dataSnapshot.getValue();
                    User p = new User(map);
                    mUsers.put(p.getID(), p);
                    if(p.getID().equals(user_id)){
                        localDataManager.updateUser(p);
                    }
                    notifyAllAdapters();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Map<String, String> map = (Map<String, String>) dataSnapshot.getValue();
                    User p = new User(map);
                    mUsers.put(p.getID(), p);
                    if(p.getID().equals(user_id)){
                        localDataManager.updateUser(p);
                    }

                    notifyAllAdapters();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                    Map<String, String> map = (Map<String, String>) dataSnapshot.getValue();
                    User p = new User(map);
                    mPosts.remove(p.getID());

                    notifyAllAdapters();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    // do nothing.
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }

            });
            mNotificationsListener = this.mNotificationsRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Notification not = Notification.buildObject(dataSnapshot.getValue());
                    mNotifications.put(not.getID(), not);
                    badgeManager.increment(BadgeManager.Tab.NOTIFICATIONS);
                    notifyAllAdapters();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Notification not = Notification.buildObject(dataSnapshot.getValue());
                    mNotifications.put(not.getID(), not);
                    notifyAllAdapters();

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Notification not = Notification.buildObject(dataSnapshot.getValue());
                    mNotifications.remove(not.getID());
                    notifyAllAdapters();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    // do nothing.
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    // do nothing.
                }
            });
            initAdapters();
        }



        public void cleanup(){
            mPostRef.removeEventListener(mPostListener);
            mUserRef.removeEventListener(mUserListener);
            mNotificationsRef.removeEventListener(mNotificationsListener);
            mPosts.clear();
            mUsers.clear();
        }

        public BaseAdapter getAdapter(AdapterType type){
            return adapterMap.get(type);
        }


        private void initAdapters(){
            adapterMap = new HashMap<>();
            adapterMap.put(AdapterType.POST_FEED, new UserFeedAdapter(activity));
            String user_id = localDataManager.getCurrentUser().getID();
            adapterMap.put(AdapterType.MY_REQUESTS, new MyRequestsAdapter(activity, user_id));
            adapterMap.put(AdapterType.PENDING_REQUESTS, new PendingRequestsAdapter(activity, user_id));

            NotificationsAdapter adapter = new NotificationsAdapter(activity, user_id);
            adapterMap.put(AdapterType.NOTIFICATIONS, adapter);

            adapterMap.put(AdapterType.CHAT, new ChatListAdapter(activity, mUsers, chatListManager.getChats()));
        }

        private void notifyAllAdapters(){
            for(BaseAdapter a : adapterMap.values()){
                a.notifyDataSetChanged();
            }
        }
    }


    public class DisplayManagerImpl implements DisplayManager{
        @Override
        public void viewPostAsOther(String viewer_id, String post_id) {
            Intent intent = new Intent(getApplicationContext(), ViewPostAsOtherActivity.class);
            intent.putExtra("post_id", post_id);
            intent.putExtra("viewer_id", viewer_id);
            startActivity(intent);
        }

        @Override
        public void viewPostAsOwner(String post_id) {
            Intent intent = new Intent(getApplicationContext(), ViewPostAsOwnerActivity.class);
            intent.putExtra("post_id", post_id);
            startActivity(intent);
        }

    }

    public class DataManagerImpl implements DataManager{
        @Override
        public synchronized Map<String, Post> getPosts() {
            return dataSync.mPosts;
        }

        @Override
        public synchronized  Map<String, User> getUsers() {
            return dataSync.mUsers;
        }

        @Override
        public synchronized Map<String, Notification> getNotifications() {
            return dataSync.mNotifications;
        }
    }

    public class ChatListManager{
        private ChatActivity currentChat;
        private String user_id;
        private String CHAT_FILE;
        Map<String, Chat> chatMap;
        SharedPreferences mPrefs;
        Firebase messagesRef;

        public ChatListManager(){
            User currentUser = localDataManager.getCurrentUser();
            if(currentUser != null) user_id = currentUser.getID();
            CHAT_FILE = "chat_file_" + user_id;
            mPrefs = getSharedPreferences(CHAT_FILE, MODE_PRIVATE);
            Set<String> keySet = mPrefs.getAll().keySet();
            chatMap = new HashMap<>();
            for(String str : keySet){
                try{
                    chatMap.put(str, new Chat(mPrefs.getString(str, null)));
                } catch(Chat.IllegalParameter e){
                    Log.e("ChatListener", e.getMessage());
                }

            }

            messagesRef = comm.getMessagesRef(user_id);
            messagesRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    ChatMessage chatMessage = new ChatMessage((Map<String, String>) dataSnapshot.getValue());
                    addMessage(chatMessage.getAuthorID(), chatMessage);
                    badgeManager.increment(BadgeManager.Tab.CHAT);
                    messagesRef.child(dataSnapshot.getKey()).removeValue();
                    if(currentChat != null){
                        currentChat.notifyDataSetChanged();
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });

        }

        /**
         *  addMessage:
         * @param id - id of other user, i.e., not device owner.
         * @param message - message to be added to chat.
         */
        public void addMessage(String id, ChatMessage message){
            Chat chat = new Chat();
            try{
                chat = new Chat(mPrefs.getString(id, null));
            } catch(Chat.IllegalParameter e){
                chat.setOtherUserID(id);
            }

            chat.addMessage(message);
            mPrefs.edit().putString(id, chat.toJson()).apply();
            chatMap.put(id, chat);
        }

        public void deleteChat(String id){
            mPrefs.edit().remove(id).apply();
            chatMap.remove(id);
        }

        public Chat getChat(String id){
            Chat retval = chatMap.get(id);
            if(retval == null){
                retval = new Chat();
                retval.setOtherUserID(id);
                chatMap.put(id, retval);
            }
            return retval;
        }

        public Map<String, Chat> getChats(){
            return chatMap;
        }

        public void setCurrentChat(ChatActivity chatActivity){
            currentChat = chatActivity;
        }
    }

    public static ChatListManager getChatListManager(){
        return chatListManager;
    }

    public static MainActivity getInstance(){
        return (MainActivity) context;
    }


}
