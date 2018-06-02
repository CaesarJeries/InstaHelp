package project.com.instahelp.chat;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class Chat implements Serializable{
    private List<ChatMessage> messages;

    /**
     * id of other user, i.e, not device owner.
     */
    private String other_id;


    public Chat(String objectStringRep) throws IllegalParameter{
        if(objectStringRep == null){
            throw new IllegalParameter();
        }
        Gson gson = new Gson();
        Chat chatObj = gson.fromJson(objectStringRep, Chat.class);
        messages = chatObj.getMessages();
        other_id = chatObj.getOtherUserID();
    }

    public Chat(){
        messages = new LinkedList<>();
        other_id = null;
    }

    public class IllegalParameter extends Exception{
        @Override
        public String getMessage() {
            return "Received null pointer in class constructor";
        }
    }

    public String toJson(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }


    public void addMessage(ChatMessage message){
        messages.add(message);
    }

    public String getOtherUserID(){
        return other_id;
    }

    public void setOtherUserID(String id){
        other_id = id;
    }

    public List<ChatMessage> getMessages(){
        return messages;
    }
}
