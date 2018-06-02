package project.com.instahelp.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import project.com.instahelp.R;
import project.com.instahelp.chat.ChatActivity;
import project.com.instahelp.interfaces.DataManager;
import project.com.instahelp.interfaces.LocalDataManager;
import project.com.instahelp.interfaces.LocationManager;
import project.com.instahelp.utils.Post;
import project.com.instahelp.utils.ServerComm;
import project.com.instahelp.utils.User;
import project.com.instahelp.utils.notifications.HelpOffer;
import project.com.instahelp.utils.notifications.Request;


public class ViewPostAsOtherActivity extends AppCompatActivity {

    private ServerComm comm;
    private Toolbar toolbar;
    private Button offerHelpButton;
    private Post post;
    private String viewer_id;
    private DataManager dataManager = MainActivity.getDataManager();
    private LocationManager locationManager;
    private LocalDataManager localDataManager = MainActivity.getLocalDataManager();
    private User currentUser;

    private View.OnClickListener offerHelpClickListener;
    private View.OnClickListener cancelHelpClickListener;
    Map<String, User> userMap;

    final static private int REQUEST_PERMISSIONS_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post_as_other);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        String post_id = getIntent().getStringExtra("post_id");
        dataManager = MainActivity.getDataManager();
        post = dataManager.getPosts().get(post_id);
        viewer_id = getIntent().getStringExtra("viewer_id");
        User u = dataManager.getUsers().get(post.getUserId());
        Bitmap userPicture = u.getProfilePicture();
        initComm();
        userMap = dataManager.getUsers();

        locationManager = new MainActivity();
        currentUser = localDataManager.getCurrentUser();
        initUI(userPicture);



    }


    private void initUI(Bitmap pic){
        final Post p = post;
        offerHelpButton = (Button) findViewById(R.id.help_button);
        ImageButton phoneButton = (ImageButton) findViewById(R.id.phone_button);
        ImageButton chatButton = (ImageButton) findViewById(R.id.chat_button);
        TextView title = (TextView) findViewById(R.id.post_title);
        TextView user_name = (TextView) findViewById(R.id.user_name);
        TextView content = (TextView) findViewById(R.id.post_content);
        TextView post_id = (TextView) findViewById(R.id.post_id);
        final TextView user_id = (TextView) findViewById(R.id.user_id);
        TextView locationView = (TextView) findViewById(R.id.post_location);
        TextView distanceView = (TextView) findViewById(R.id.post_distance);
        View locationLayout = findViewById(R.id.location_layout);
        View distanceLayout = findViewById(R.id.distance_layout);
        ImageView profilePic = (ImageView) findViewById(R.id.profile_pic);

        profilePic.setImageBitmap(pic);

        title.setText(p.getTitle());
        user_name.setText(p.getUserName());
        content.setText(p.getContent());
        post_id.setText(p.getID());
        user_id.setText(p.getUserId());

        if(p.isLocationPrivate()){
            distanceLayout.setVisibility(View.VISIBLE);
            distanceView.setText(String.valueOf(calculateDistance(p.getLocation())));
        }else{
            String address = getAddress(p.getLocation());
            if(address != null){
                locationLayout.setVisibility(View.VISIBLE);
                locationView.setText(address);
            }

        }

        final String viewer_id = currentUser.getID();

        phoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean hasPermission = p.hasPermission(viewer_id, Post.Permission.PhoneNumber);
                if(p.isPhoneNumberPrivate() && (hasPermission == false)){
                    showPhoneHandshakeDialog();
                }else{
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + p.getPhoneNumber()));
                    startActivity(intent);
                }
            }
        });

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra("other_id", post.getUserId());
                startActivity(intent);
            }
        });

        switch(p.getStatus()){
            case Assigned:
                offerHelpButton.setVisibility(View.GONE);
                View chosen_user_view = findViewById(R.id.chosen_user_view);
                TextView chosen_user_tv = (TextView) findViewById(R.id.chosen_user_id);
                chosen_user_view.setVisibility(View.VISIBLE);
                String chosenName = userMap.get(post.getChosenUserId()).getName();
                chosen_user_tv.setText(chosenName);
                if(viewer_id.equals(p.getChosenUserId())){
                    Button addToCalendar = (Button) findViewById(R.id.export_to_calendar_button);
                    addToCalendar.setVisibility(View.VISIBLE);
                    addToCalendar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            addEventToCalendar(p);
                        }
                    });
                }
                break;
            case NotAssigned:
                offerHelpButton.setVisibility(View.VISIBLE);
        }

        initButtonListeners();
        setUpOfferHelpButton();
    }


    private void initButtonListeners(){
        offerHelpClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user_id = currentUser.getID();
                HelpOffer helpOffer = new HelpOffer("0", post.getID(), user_id, post.getUserId());
                comm.addNotification(helpOffer);
                comm.addHelpOffer(post.getID(), user_id);
                String text = getResources().getString(R.string.view_post_cancel_help_offer);
                offerHelpButton.setText(text);
                offerHelpButton.setOnClickListener(cancelHelpClickListener);
            }
        };

        cancelHelpClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comm.removeHelpOffer(post.getID(), viewer_id);
                String text = getResources().getString(R.string.view_post_offer_help);
                offerHelpButton.setText(text);
                offerHelpButton.setOnClickListener(offerHelpClickListener);
            }
        };
    }



    private void setUpOfferHelpButton(){
        if(post.getStatus() == Post.Status.Assigned){
            offerHelpButton.setVisibility(View.GONE);
            return;
        }

        if(post.userOfferedHelp(viewer_id)){
            String text = getResources().getString(R.string.view_post_cancel_help_offer);
            offerHelpButton.setText(text);
            offerHelpButton.setOnClickListener(cancelHelpClickListener);
        }else{
            String text = getResources().getString(R.string.view_post_offer_help);
            offerHelpButton.setText(text);
            offerHelpButton.setOnClickListener(offerHelpClickListener);
        }

    }


    private int calculateDistance(Map<String, String> location){
        Location myLocation = locationManager.getCurrentLocation();
        Location postLocation = new Location("point B");
        postLocation.setLatitude(Double.parseDouble(location.get("lat")));
        postLocation.setLongitude(Double.parseDouble(location.get("long")));

        if(myLocation != null) return (int) myLocation.distanceTo(postLocation);

        return 0;
    }


    private String getAddress(Map<String, String> location){
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        double lat = Double.valueOf(location.get("lat"));
        double longitude = Double.valueOf(location.get("long"));
        List<Address> list = null;
        try{
            list = geocoder.getFromLocation(lat, longitude, 1);
        } catch(IOException e){
            Log.e("ViewPostOther", e.getMessage());
        }
        String result = null;
        if (list != null & list.size() > 0) {
            Address address = list.get(0);
            result = address.getFeatureName() + ", " + address.getLocality() + ", " + address.getCountryName();
        }
        return result;
    }



    private void showPhoneHandshakeDialog(){
        String title = getResources().getString(R.string.phone_handshake_alert_title);
        String content = getResources().getString(R.string.phone_handshake_alert_content);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        initiatePhoneHandshake();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void initiatePhoneHandshake(){
        Request request = new Request("0", post.getID(), viewer_id, post.getUserId());
        comm.addNotification(request);
    }


    private int getMonth(String month){
        String[] dfs = new DateFormatSymbols().getMonths();
        for(int i = 0; i < 12 ; i++){
            if(dfs[i].equals(month)) return i+1;
        }
        dfs = new DateFormatSymbols(Locale.US).getMonths();
        for(int i = 0; i < 12 ; i++){
            if(dfs[i].equals(month)) return i+1;
        }
        return 0;
    }

    private void addEvent(Post post){
        Calendar beginTime = Calendar.getInstance();
        String[] date = post.getDate().split("/");
        int year = Integer.valueOf(date[0]);
        int month = getMonth(date[1]);
        int day = Integer.valueOf(date[2]);
        String[] time = post.getTime().split(":");
        int hour = Integer.valueOf(time[0]);
        int minute = Integer.valueOf(time[1]);

        beginTime.set(year, month, day, hour, minute);
        long startMillis = beginTime.getTimeInMillis();

        Calendar endTime = Calendar.getInstance();
        endTime.set(year, month, day, hour + 1, minute);
        long endMillis = endTime.getTimeInMillis();

        Intent calIntent = new Intent(Intent.ACTION_INSERT);
        calIntent.setType("vnd.android.cursor.item/event");
        calIntent.putExtra(CalendarContract.Events.TITLE, post.getTitle());
        calIntent.putExtra(CalendarContract.Events.DESCRIPTION, post.getContent());
        calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis);
        calIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis);
        startActivity(calIntent);
    }


    private void addEventToCalendar(Post post){
        boolean added = false;
        int permissionCheck;
        String[] permissions = new String[1];
        permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR);
        if(permissionCheck == PackageManager.PERMISSION_DENIED){
            permissions[0] = Manifest.permission.WRITE_CALENDAR;
            added = true;
        }

        if(added){
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS_CODE);
        }else{
            addEvent(post);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSIONS_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    addEvent(post);

                } else {

                    Toast.makeText(this, "Add to calendar failed: Permissions not granted", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }

    private void initComm(){
        try{
            comm = ServerComm.getInstance();
        }catch (ServerComm.AppNotInitializedException e){
            Log.e("Post class", e.getMessage());
        }
    }
}
