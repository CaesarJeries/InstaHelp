package project.com.instahelp.utils.notifications;

public class HelpOffer extends Notification {

    public HelpOffer(String id, String post_id, String from, String to){
        super(id, post_id, from, to);
        setType(Type.HelpOffer);
    }
}
