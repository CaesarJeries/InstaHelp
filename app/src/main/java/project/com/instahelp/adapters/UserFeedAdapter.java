package project.com.instahelp.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import project.com.instahelp.R;
import project.com.instahelp.activities.MainActivity;
import project.com.instahelp.interfaces.DataManager;
import project.com.instahelp.interfaces.LocationManager;
import project.com.instahelp.utils.Post;
import project.com.instahelp.utils.User;
import utils.gui.CircleImageView;


public class UserFeedAdapter extends BaseAdapter{

    // radius in meters
    private static final int POSTS_RADIUS = 1250;

    private Activity activity;
    private int mLayout;
    private LayoutInflater mInflater;
    DataManager dataManager = MainActivity.getDataManager();

    private LocationManager locationManager;

    public UserFeedAdapter(Activity activity) {
        mLayout = R.layout.row_post;
        mInflater = activity.getLayoutInflater();
        this.activity = activity;
        locationManager = (LocationManager) activity;
    }




    @Override
    public int getCount() {
        return filterPosts(dataManager.getPosts()).size();
    }

    @Override
    public Object getItem(int i) {
        List<Post> snapshot = new LinkedList<>();
        snapshot.addAll(filterPosts(dataManager.getPosts()));
        Post model = snapshot.get(i);
        return model;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = mInflater.inflate(mLayout, viewGroup, false);
        }

        List<Post> snapshot = filterPosts(dataManager.getPosts());

        if(snapshot.size() > 0){
            Post model = snapshot.get(i);
            // Call out to subclass to marshall this model into the provided view
            populateView(view, model);
        }


        return view;
    }

    private class PostComparator implements Comparator<Post>{
        @Override
        public int compare(Post lhs, Post rhs) {
            int lhs_count = dataManager.getUsers().get(lhs.getUserId()).getHelpCount();
            int rhs_count = dataManager.getUsers().get(rhs.getUserId()).getHelpCount();
            if(lhs_count > rhs_count) return 1;
            if(rhs_count == lhs_count) return 0;
            return -1;
        }

        @Override
        public boolean equals(Object object) {
            return false;
        }
    }

    private List<Post> filterPosts(Map<String, Post> map){
        List<Post> retval = new LinkedList<>();
        for(Map.Entry<String, Post> itr : map.entrySet()){
            int dist = calculateDistance(itr.getValue().getLocation());
            if(dist > 0 && dist <= POSTS_RADIUS){
                retval.add(itr.getValue());
            }
        }

        Collections.sort(retval, new PostComparator());
        return retval;
    }

    private int calculateDistance(Map<String, String> location){
        Location myLocation = locationManager.getCurrentLocation();
        Location postLocation = new Location("point B");
        postLocation.setLatitude(Double.parseDouble(location.get("lat")));
        postLocation.setLongitude(Double.parseDouble(location.get("long")));
        if(myLocation != null){
            return (int) myLocation.distanceTo(postLocation);
        }else{
            return -1;
        }

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
