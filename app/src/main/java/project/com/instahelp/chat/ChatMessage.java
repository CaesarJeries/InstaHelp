package project.com.instahelp.chat;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ChatMessage implements Serializable{
    private String authorID;
    private String message;

    public ChatMessage(String author_id, String message){
        this.authorID = author_id;
        this.message = message;
    }

    public ChatMessage(Map<String, String> map){
        authorID = map.get("authorID");
        message = map.get("message");
    }

    public String getAuthorID(){
        return authorID;
    }

    public String getMessage(){
        return message;
    }

    public Map<String, String> toMap(){
        Map<String, String> retval = new HashMap<>();
        retval.put("authorID", authorID);
        retval.put("message", message);
        return retval;
    }
}
