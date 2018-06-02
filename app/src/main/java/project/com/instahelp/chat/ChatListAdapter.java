package project.com.instahelp.chat;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import project.com.instahelp.R;
import project.com.instahelp.utils.User;
import utils.gui.CircleImageView;

public class ChatListAdapter extends BaseAdapter{

    private Activity activity;
    private int mLayout = R.layout.row_conversation;
    private LayoutInflater mInflater;
    private Map<String, User> mUsers; // required for fetching profile picture.
    private Map<String, Chat> mChats;


    public ChatListAdapter(Activity activity, Map<String, User> users, Map<String, Chat> chats){
        this.activity = activity;
        mInflater = activity.getLayoutInflater();
        mUsers = users;
        mChats = chats;
    }


    @Override
    public int getCount() {
        if(mUsers.isEmpty()) return 0;
        return mChats.size();
    }

    @Override
    public Object getItem(int position) {
        List<Chat> list = new LinkedList<>();
        list.addAll(mChats.values());
        Chat retval = list.get(position);
        if(retval.getMessages().isEmpty()){
            return null;
        }
        return retval;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        Chat chat = (Chat) getItem(position);

        if (view == null) {
            view = mInflater.inflate(mLayout, viewGroup, false);
        }
        populateView(view, chat);
        return view;
    }

    private void populateView(View view, Chat chat){
        TextView userNameView = (TextView) view.findViewById(R.id.user_name);
        TextView userIdView = (TextView) view.findViewById(R.id.user_id);
        CircleImageView profilePictureView = (CircleImageView) view.findViewById(R.id.profile_picture);

        userIdView.setText(chat.getOtherUserID());
        User u = mUsers.get(chat.getOtherUserID());
        userNameView.setText(u.getName());
        profilePictureView.setImageBitmap(u.getProfilePicture());
    }

}
