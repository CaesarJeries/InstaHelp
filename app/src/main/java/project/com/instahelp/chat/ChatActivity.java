package project.com.instahelp.chat;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import project.com.instahelp.R;
import project.com.instahelp.activities.MainActivity;
import project.com.instahelp.interfaces.LocalDataManager;
import project.com.instahelp.utils.ServerComm;
import project.com.instahelp.utils.User;
import utils.gui.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    MainActivity.ChatListManager chatListManager = MainActivity.getChatListManager();
    LocalDataManager localDataManager = MainActivity.getLocalDataManager();
    String other_id;
    String my_id;
    EditText messageEdit;
    ServerComm comm;
    MessageAdapter adapter;
    Chat chat;
    ListView messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setCustomView(R.layout.chat_activity_action_bar);

        other_id = getIntent().getStringExtra("other_id");
        my_id = localDataManager.getCurrentUser().getID();

        TextView userNameView = (TextView) bar.getCustomView().findViewById(R.id.user_name);
        CircleImageView pictureView = (CircleImageView) bar.getCustomView().findViewById(R.id.profile_picture);

        User u = MainActivity.getDataManager().getUsers().get(other_id);
        userNameView.setText(u.getName());
        pictureView.setImageBitmap(u.getProfilePicture());
        bar.setDisplayShowCustomEnabled(true);
        bar.setTitle("");

        initComm();
        initUI();
        chatListManager.setCurrentChat(this);
    }

    public void notifyDataSetChanged(){
        chat = chatListManager.getChat(other_id);
        adapter.setMessages(chat.getMessages());
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onSupportNavigateUp() {
        boolean retval =  super.onSupportNavigateUp();
        finish();
        return retval;
    }

    private void initUI(){
        messages = (ListView) findViewById(R.id.message_view);
        chat = chatListManager.getChat(other_id);
        this.adapter = new MessageAdapter(this, chat.getMessages());
        //final MessageAdapter adapter = this.adapter;

        if(messages != null) messages.setAdapter(adapter);

        messageEdit = (EditText) findViewById(R.id.message);
        ImageButton sendButton = (ImageButton) findViewById(R.id.send_button);

        if(sendButton != null )
            sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageEdit.getText().toString();
                if(!message.isEmpty()){
                    ChatMessage chatMessage = new ChatMessage(my_id, message);
                    chatListManager.addMessage(other_id, chatMessage);
                    messageEdit.setText(null);
                    comm.sendMessage(other_id, chatMessage);
                    adapter.add(chatMessage);
                    adapter.notifyDataSetChanged();
                }
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


}
