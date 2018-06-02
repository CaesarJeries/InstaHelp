package project.com.instahelp.adapters;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import project.com.instahelp.R;
import project.com.instahelp.activities.MainActivity;
import project.com.instahelp.activities.ViewPostAsOwnerActivity;
import project.com.instahelp.interfaces.DataManager;
import project.com.instahelp.utils.Post;
import project.com.instahelp.utils.ServerComm;
import project.com.instahelp.utils.User;
import project.com.instahelp.utils.notifications.Notification;
import project.com.instahelp.utils.notifications.Response;
import utils.gui.CircleImageView;


public class NotificationsAdapter extends BaseAdapter {

    private String user_id;
    private Activity activity;

    private static final int REQUEST_LAYOUT = R.layout.row_request;
    private static final int RESPONSE_LAYOUT = R.layout.row_response;

    private LayoutInflater mInflater;
    private DataManager dataManager = MainActivity.getDataManager();

    ServerComm comm;


    public NotificationsAdapter(Activity activity, String user_id){
        this.user_id = user_id;
        this.activity = activity;
        mInflater = activity.getLayoutInflater();
        initComm();
    }


    @Override
    public int getCount() {
        return dataManager.getNotifications().size();
    }

    @Override
    public Object getItem(int position) {
        List<Notification> snapshot = new LinkedList<>();
        snapshot.addAll(dataManager.getNotifications().values());
        return snapshot.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflateRow(i, viewGroup);
        }

        List<Notification> snapshot = new LinkedList<>();
        snapshot.addAll(dataManager.getNotifications().values());
        if(snapshot.size() > 0){
            Notification model = snapshot.get(i);
            populateView(view, model);
        }


        return view;
    }



    /**
     * Private methods
     */



    private void populateView(View v, final Notification not) {

        CircleImageView profile_picture_v = (CircleImageView) v.findViewById(R.id.profile_picture);
        TextView user_name_v = (TextView) v.findViewById(R.id.user_name);
        TextView user_id_v = (TextView) v.findViewById(R.id.user_id);
        TextView post_id_v = (TextView) v.findViewById(R.id.post_id);
        TextView notification_type_v = (TextView) v.findViewById(R.id.notification_type);
        TextView post_title_v = (TextView) v.findViewById(R.id.post_title);
        TextView notification_content = (TextView) v.findViewById(R.id.content);

        final String post_id = not.getPostID();
        post_id_v.setText(post_id);
        Post p = dataManager.getPosts().get(post_id);
        if(p == null) return;
        String sender_id = not.getSenderID();
        final User u = dataManager.getUsers().get(sender_id);
        if(u == null) return;
        post_title_v.setText(p.getTitle());
        notification_type_v.setText(not.getType().toString());

        String content = getContent(not);
        notification_content.setText(content);
        switch(not.getType()){
            case Response:{
                profile_picture_v.setImageBitmap(u.getProfilePicture());
                user_id_v.setText(u.getID());
                user_name_v.setText(u.getName());


                break;
            }

            case HelpOffer:{
                profile_picture_v.setImageBitmap(u.getProfilePicture());
                user_id_v.setText(u.getID());
                user_name_v.setText(u.getName());
                break;
            }

            case Request:{
                profile_picture_v.setImageBitmap(u.getProfilePicture());
                user_id_v.setText(u.getID());
                user_name_v.setText(u.getName());
                ImageButton acceptButton = (ImageButton) v.findViewById(R.id.accept_button);
                ImageButton declineButton = (ImageButton) v.findViewById(R.id.decline_button);
                acceptButton.setTag(u.getID());
                declineButton.setTag(u.getID());
                acceptButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        comm.addPermission(post_id, u.getID(), Post.Permission.PhoneNumber);
                        comm.deleteNotification(user_id, not.getID());
                        Response response = new Response("0", post_id, user_id, u.getID(), Response.Permission.Granted);
                        comm.addNotification(response);
                        notifyDataSetChanged();
                    }
                });
                declineButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        comm.deleteNotification(user_id, not.getID());
                        Response response = new Response("0", post_id, user_id, u.getID(), Response.Permission.Denied);
                        response.setPermission(Response.Permission.Denied);
                        comm.addNotification(response);
                        notifyDataSetChanged();
                    }
                });

                break;
            }
        }

        ImageButton deleteButton = (ImageButton) v.findViewById(R.id.delete_button);
        final String id = user_id;
        if(deleteButton != null){ // request layout doesn't have a delete button.
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    comm.deleteNotification(id, not.getID());
                }
            });
        }

        post_title_v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity.getApplicationContext(), ViewPostAsOwnerActivity.class);
                intent.putExtra("post_id", post_id);
                activity.startActivity(intent);

            }
        });

    }


    private void initComm(){
        try{
            comm = ServerComm.getInstance();
        }catch (ServerComm.AppNotInitializedException e){
            Log.e("Firebase", e.getMessage());
        }
    }


    private View inflateRow(int index, ViewGroup viewGroup){
        int mLayout;

        List<Notification> snapshot = new LinkedList<>();
        snapshot.addAll(dataManager.getNotifications().values());
        if(snapshot.get(index).getType() == Notification.Type.Request){
            mLayout = REQUEST_LAYOUT;
        }else{
            mLayout = RESPONSE_LAYOUT;
        }
        return mInflater.inflate(mLayout, viewGroup, false);
    }


    private String getContent(Notification not){
        String retval = null;
        switch(not.getType()){
            case Request:{
                retval = activity.getString(R.string.request_phone);
                break;
            }
            case Response:{
                Response response = (Response) not;
                if(response.getPermission() == Response.Permission.Granted){
                    retval = activity.getString(R.string.response_phone_granted);
                }else{
                    retval = activity.getString(R.string.response_phone_denied);
                }
                break;
            }
            case HelpOffer:{
                retval = activity.getString(R.string.help_offer_notification);
                break;
            }
        }

        return retval;
    }

}
