package project.com.instahelp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Map;

import project.com.instahelp.R;
import project.com.instahelp.fragments.HelpOffersFragment;
import project.com.instahelp.interfaces.DataManager;
import project.com.instahelp.utils.Defaults;
import project.com.instahelp.utils.Post;
import project.com.instahelp.utils.ServerComm;
import project.com.instahelp.utils.User;


public class ViewPostAsOwnerActivity extends AppCompatActivity {


    private TextView post_title_tv;
    private TextView post_content_tv;
    private TextView post_help_offers_tv;
    private static Post post;
    private ServerComm comm;
    private Toolbar toolbar;
    private Map<String, User> usersMap;
    private DataManager dataManager = MainActivity.getDataManager();
    private String post_id;
    private View chosenUserLayout;
    private TextView chosenUserView;
    private View dateLayout;
    private View timeLayout;
    private TextView dateView;
    private TextView timeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post_as_owner);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        dataManager = MainActivity.getDataManager();
        if(post == null){
            post_id = getIntent().getStringExtra("post_id");
            post = dataManager.getPosts().get(post_id);
        }

        usersMap = dataManager.getUsers();
        try{
            comm = ServerComm.getInstance();
        }catch (ServerComm.AppNotInitializedException e){
            Log.e("View Post", e.getMessage());
        }
        initUI(post);
    }

    private void initUI(Post post){
        post_title_tv = (TextView) findViewById(R.id.view_post_as_owner_title);
        post_content_tv = (TextView) findViewById(R.id.view_post_as_owner_content);
        post_help_offers_tv = (TextView) findViewById(R.id.view_post_as_owner_helpOffers);
        post_title_tv.setText(post.getTitle());
        post_content_tv.setText(post.getContent());
        chosenUserLayout = findViewById(R.id.chosen_user_layout);
        chosenUserView = (TextView) findViewById(R.id.chosen_user);
        dateLayout = findViewById(R.id.date_layout);
        timeLayout = findViewById(R.id.time_layout);
        dateView = (TextView) findViewById(R.id.date);
        timeView = (TextView) findViewById(R.id.time);

        setDateAndTime(post);

        Map<String, String> helpOffers = post.getHelpOffers();

        String peopleOfferedHelp = getString(R.string.view_post_people_offered_help);
        switch(post.getStatus()){
            case NotAssigned:{
                if(helpOffers != null){
                    post_help_offers_tv.setText(post.getHelpOffers().size() + " " + peopleOfferedHelp );
                    if(post.getHelpOffers().size() > 0){
                        post_help_offers_tv.setEnabled(true);
                    }else{
                        post_help_offers_tv.setEnabled(false);
                    }

                }else{
                    post_help_offers_tv.setText("0 " + peopleOfferedHelp );
                    post_help_offers_tv.setEnabled(false);
                }
                break;
            }

            case Assigned:{
                User chosenUser = usersMap.get(post.getChosenUserId());
                post_help_offers_tv.setVisibility(View.GONE);
                chosenUserLayout.setVisibility(View.VISIBLE);
                chosenUserView.setText(chosenUser.getName());
                final Post p = post;
                chosenUserView.setEnabled(true);
                chosenUserView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(getApplicationContext(), ViewProfileAsOtherActivity.class);
                        intent.putExtra(Defaults.PreferencesTags.USER_ID, p.getChosenUserId());
                        intent.putExtra("post_id", p.getID());
                        startActivity(intent);
                    }
                });
                break;
            }
        }


    }

    private void setDateAndTime(Post p){
        timeView.setText(p.getTime());
        dateView.setText(p.getDate());
    }
    public void deletePostClicked(View view){
        // comm.deletePost(post.getID());
        final Intent intent = new Intent(this , MainActivity.class);
        String title = getResources().getString(R.string.delete_post_alert_title);
        String content = getResources().getString(R.string.delete_post_alert_content);
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        comm.deletePost(post.getID());
                        comm.deleteRelatedNotifications(post.getID(), post.getUserId());
                        startActivity(intent);
                        finish();
                    }

                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();

    }

    public void markDonePressed(View view){
        String chosen_user = post.getChosenUserId();
        if(chosen_user != null){
            comm.incrementHelpCount(chosen_user);
        }
        comm.deletePost(post.getID());
    }


    public void showHelpOffers(View v){
        HelpOffersFragment frag = new HelpOffersFragment();
        frag.setArgument(post);
        frag.setArgument(usersMap);
        frag.show(getSupportFragmentManager(), "helpOffersDialog");
    }


    public void viewProfileAsOther(String profile_owner) {
        Intent intent = new Intent(getApplicationContext(), ViewProfileAsOtherActivity.class);
        intent.putExtra(Defaults.PreferencesTags.USER_ID, profile_owner);
        startActivity(intent);
    }

    public void setChosenUser(String userName, final String userID){
        post_help_offers_tv.setVisibility(View.GONE);
        chosenUserLayout.setVisibility(View.VISIBLE);
        chosenUserView.setText(userName);
        post_help_offers_tv.invalidate();
        chosenUserView.invalidate();
        chosenUserView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ViewProfileAsOtherActivity.class);
                intent.putExtra(Defaults.PreferencesTags.USER_ID, userID);
                startActivity(intent);
            }
        });
    }

    public void refreshHelpOffers(){
        String peopleOfferedHelp = getString(R.string.view_post_people_offered_help);
        post_help_offers_tv.setText(post.getHelpOffers().size() + " " + peopleOfferedHelp );
        if(post.getHelpOffers().size() > 0){
            post_help_offers_tv.setEnabled(true);
        }else{
            post_help_offers_tv.setEnabled(false);
        }
        post_help_offers_tv.invalidate();
    }

}
