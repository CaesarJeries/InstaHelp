package project.com.instahelp.chat;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import project.com.instahelp.R;
import project.com.instahelp.activities.MainActivity;
import project.com.instahelp.interfaces.LocalDataManager;


public class MessageAdapter extends BaseAdapter {

    Activity activity;
    List<ChatMessage> messages;
    LayoutInflater mInflater;
    LocalDataManager localDataManager = MainActivity.getLocalDataManager();

    public MessageAdapter(Activity activity, List<ChatMessage> messages){
        this.activity = activity;
        this.messages = messages;
        if(messages == null) this.messages = new LinkedList<>();
        mInflater = activity.getLayoutInflater();
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        int mLayout;
        ChatMessage message = (ChatMessage) getItem(position);
        String user_id = localDataManager.getCurrentUser().getID();

        if(user_id.equals(message.getAuthorID())){
            mLayout = R.layout.message_right;
        }else{
            mLayout = R.layout.message_left;
        }

        // re-inflate view
        view = mInflater.inflate(mLayout, viewGroup, false);

        populateView(view, message);


        return view;
    }

    public void add(ChatMessage message){
        messages.add(message);
    }


    public void setMessages(List<ChatMessage> list){
        messages = list;
    }
    private void populateView(View v, ChatMessage message){
        TextView messageView = (TextView) v.findViewById(R.id.message);
        messageView.setText(message.getMessage());
    }

}
