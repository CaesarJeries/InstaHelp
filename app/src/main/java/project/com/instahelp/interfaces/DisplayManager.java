package project.com.instahelp.interfaces;

public interface DisplayManager {
    void viewPostAsOther(String viewer_id, String post_id);
    void viewPostAsOwner(String post_id);
}
