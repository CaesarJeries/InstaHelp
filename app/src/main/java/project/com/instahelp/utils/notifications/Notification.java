package project.com.instahelp.utils.notifications;


import java.util.HashMap;
import java.util.Map;

public class Notification {


    public enum Type{
        Response,
        Request,
        HelpOffer
    }

    protected Type type;
    protected String id;
    protected String post_id;
    protected String sender_id;
    protected String receiver_id;


    public Notification(String id, String post_id, String from, String to){
        this.id = id;
        this.post_id = post_id;
        sender_id = from;
        receiver_id = to;
    }

    public Notification(Map<String, String> map){
        id = map.get("id");
        post_id = map.get("post_id");
        sender_id = map.get("sender_id");
        receiver_id = map.get("receiver_id");
        type = Type.valueOf(map.get("type"));
    }



    public String getID(){
        return id;
    }

    public void setID(String id){
        this.id = id;
    }

    public Type getType(){
        return type;
    }

    public void setType(Type type){
        this.type = type;
    }

    public String getPostID(){
        return post_id;
    }

    public String getSenderID(){
        return sender_id;
    }

    public String getReceiverID(){
        return receiver_id;
    }

    public Map<String, String> toMap(){
        Map<String, String> retval = new HashMap<>();
        retval.put("id", id);
        retval.put("type", type.toString());
        retval.put("post_id", post_id);
        retval.put("sender_id", sender_id);
        retval.put("receiver_id", receiver_id);
        return retval;
    }

    public static Notification buildObject(Object object){
        Map<String, String> map = (Map<String, String>) object;
        Notification retval = null;
        Type type = Type.valueOf(map.get("type"));
        String id = map.get("id");
        String post_id = map.get("post_id");
        String sender_id = map.get("sender_id");
        String receiver_id = map.get("receiver_id");
        switch(type){
            case Request:{
                retval = new Request(id, post_id, sender_id, receiver_id);
                break;
            }
            case Response:{
                Response.Permission permission = Response.Permission.valueOf(map.get("permission"));
                retval = new Response(id, post_id, sender_id, receiver_id, permission);
                break;
            }
            case HelpOffer:{
                retval = new HelpOffer(id, post_id, sender_id, receiver_id);
                break;
            }
        }
        return retval;
    }

}
