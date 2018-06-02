package project.com.instahelp.adapters;


import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import project.com.instahelp.R;
import project.com.instahelp.activities.MainActivity;
import project.com.instahelp.activities.ViewPostAsOwnerActivity;
import project.com.instahelp.utils.Post;
import project.com.instahelp.utils.ServerComm;
import project.com.instahelp.utils.User;
import utils.gui.CircleImageView;

public class HelpOffersViewAdapter extends BaseAdapter{

    private Activity activity;

    private int mLayout;
    private LayoutInflater mInflater;
    private View.OnClickListener positiveButtonListener;
    private View.OnClickListener negativeButtonListener;

    private final Map<String, User> userMap;
    private Post post;

    private ServerComm comm;



    public HelpOffersViewAdapter(final DialogFragment dialogFragment, final Post post, final Map<String, User> userMap, Activity activity){
        this.post = post;
        this.userMap = userMap;
        this.activity = activity;
        mLayout = R.layout.row_help_offer;
        mInflater = activity.getLayoutInflater();

        try{
            comm = ServerComm.getInstance();
        } catch(ServerComm.AppNotInitializedException e){
            Log.e("HelpOffersAdapter", e.getMessage());
        }

        positiveButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comm.setChosenUser(post.getID(), (String) v.getTag());
                String userName = userMap.get(v.getTag()).getName();
                String user_id = userMap.get(v.getTag()).getID();
                ((ViewPostAsOwnerActivity) dialogFragment.getActivity()).setChosenUser(userName, user_id);
                dialogFragment.dismiss();
            }
        };

        negativeButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sender_id = (String) v.getTag();
                comm.deleteNotification(sender_id, MainActivity.getLocalDataManager().getCurrentUser().getID(), post.getID());
                comm.removeHelpOffer(post.getID(), (String) v.getTag());
                ((ViewPostAsOwnerActivity) dialogFragment.getActivity()).refreshHelpOffers();
                dialogFragment.dismiss();
            }
        };



    }


    @Override
    public int getCount() {
        if(post.getHelpOffers() != null){
            return post.getHelpOffers().size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if(post.getHelpOffers() != null){
            List<String> snapshot = new LinkedList<>();
            snapshot.addAll(post.getHelpOffers().values());
            return snapshot.get(position);
        }
        return null;
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

        List<String> snapshot = new LinkedList<>();
        snapshot.addAll(post.getHelpOffers().values());
        if(snapshot.size() > 0){
            String user_id = snapshot.get(position);
            // Call out to subclass to marshall this model into the provided view
            populateView(view, userMap.get(user_id));
        }


        return view;
    }

    /**
     * Private methods
     */



    private void populateView(View v, User user) {
        TextView user_id_view = (TextView) v.findViewById(R.id.user_id);
        CircleImageView profile_pic_view = (CircleImageView) v.findViewById(R.id.profile_picture);
        TextView user_name_view = (TextView) v.findViewById(R.id.user_name);

        user_id_view.setText(user.getID());
        profile_pic_view.setImageBitmap(user.getProfilePicture());
        user_name_view.setText(user.getName());

        ImageButton acceptButton = (ImageButton) v.findViewById(R.id.accept_button);
        ImageButton declineButton = (ImageButton) v.findViewById(R.id.decline_button);
        acceptButton.setTag(user.getID());
        declineButton.setTag(user.getID());
        acceptButton.setOnClickListener(positiveButtonListener);
        declineButton.setOnClickListener(negativeButtonListener);
    }

}
