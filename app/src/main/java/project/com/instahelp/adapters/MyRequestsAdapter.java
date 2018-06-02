package project.com.instahelp.adapters;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import project.com.instahelp.R;
import project.com.instahelp.activities.MainActivity;
import project.com.instahelp.interfaces.DataManager;
import project.com.instahelp.utils.Post;
import project.com.instahelp.utils.User;
import utils.gui.CircleImageView;

public class MyRequestsAdapter extends BaseAdapter{

    Activity activity;
    private int mLayout;
    private LayoutInflater mInflater;

    private DataManager dataManager = MainActivity.getDataManager();

    private String user_id;
    public MyRequestsAdapter(Activity activity, String user_id){
        mLayout = R.layout.row_post;
        mInflater = activity.getLayoutInflater();
        this.activity = activity;
        this.user_id = user_id;
    }


    @Override
    public int getCount() {
        return getMyPosts().size();
    }

    @Override
    public Object getItem(int position) {
        List<Post> snapshot = new LinkedList<>();
        snapshot.addAll(getMyPosts().values());
        return snapshot.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = mInflater.inflate(mLayout, viewGroup, false);
        }

        List<Post> snapshot = new LinkedList<>();
        snapshot.addAll(getMyPosts().values());
        if(snapshot.size() > 0){
            Post model = snapshot.get(position);
            // Call out to subclass to marshall this model into the provided view
            populateView(view, model);
        }


        return view;
    }

    private Map<String, Post> getMyPosts(){
        Map<String, Post> retval = new HashMap<>();
        for(Post p : dataManager.getPosts().values()){
            if(p.getUserId().compareTo(user_id) == 0){
                retval.put(p.getID(), p);
            }
        }
        return retval;
    }

    private void populateView(View v, Post post) {
        TextView title = (TextView) v.findViewById(R.id.row_post_title);
        TextView user_name = (TextView) v.findViewById(R.id.row_post_user_name);
        CircleImageView profilePicture = (CircleImageView) v.findViewById(R.id.row_post_image);

        title.setText(post.getTitle());
        user_name.setText(post.getUserName());

        String user_id = post.getUserId();
        String post_id = post.getID();
        User u = null;

        for(Map.Entry<String, User> itr : dataManager.getUsers().entrySet()){
            if(itr.getValue().getID().compareTo(user_id) == 0){
                u = itr.getValue();
                break;
            }
        }
        Bitmap default_avatar = BitmapFactory.decodeResource(activity.getResources(),
                R.drawable.default_avatar);
        if(u != null){
            if(u.getProfilePicture() != null){
                profilePicture.setImageBitmap(u.getProfilePicture());
            }else{
                profilePicture.setImageBitmap(default_avatar);
            }

        }else{

            profilePicture.setImageBitmap(default_avatar);
        }


        // initialize postId / userId fields for internal use.
        TextView userId = (TextView) v.findViewById(R.id.row_post_user_id);
        TextView postId = (TextView) v.findViewById(R.id.row_post_post_id);

        userId.setText(user_id);
        postId.setText(post_id);


    }

}
