package project.com.instahelp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import project.com.instahelp.R;
import project.com.instahelp.interfaces.DataManager;
import project.com.instahelp.utils.Defaults;
import project.com.instahelp.utils.User;
import utils.gui.CircleImageView;


public class ViewProfileAsOtherActivity extends AppCompatActivity {

    private DataManager dataManager = MainActivity.getDataManager();
    private String user_id;
    private String post_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile_as_other);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar bar = getSupportActionBar();
        if (bar != null) bar.setDisplayHomeAsUpEnabled(true);

        user_id = getIntent().getStringExtra(Defaults.PreferencesTags.USER_ID);
        post_id = getIntent().getStringExtra("post_id");
        initUI();
    }

    private void initUI(){
        User u = dataManager.getUsers().get(user_id);
        CircleImageView profilePictureView = (CircleImageView) findViewById(R.id.profile_picture);
        TextView userNameView = (TextView) findViewById(R.id.user_name);
        TextView phoneNumberView = (TextView) findViewById(R.id.phone_number);
        ImageView rankView = (ImageView) findViewById(R.id.rank);

        profilePictureView.setImageBitmap(u.getProfilePicture());
        userNameView.setText(u.getName());
        phoneNumberView.setText(u.getPhoneNumber());
        rankView.setImageBitmap(u.getRankImage(u.getRank(), getResources()));
    }


    @Override
    public boolean onSupportNavigateUp() {
        boolean retval =  super.onSupportNavigateUp();
        Intent intent = new Intent(this, ViewPostAsOwnerActivity.class);
        intent.putExtra("post_id", post_id);
        startActivity(intent);
        finish();
        return retval;
    }
}
