package project.com.instahelp.interfaces;

import java.util.Map;

import project.com.instahelp.utils.Post;
import project.com.instahelp.utils.User;
import project.com.instahelp.utils.notifications.Notification;

public interface DataManager {
    Map<String, Post> getPosts();
    Map<String, User> getUsers();
    Map<String, Notification> getNotifications();
}
