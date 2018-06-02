package project.com.instahelp.utils.notifications;

public class Request extends Notification{
    public Request(String id, String post_id, String from, String to){
        super(id, post_id, from, to);
        setType(Type.Request);
    }

}
