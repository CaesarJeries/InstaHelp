package project.com.instahelp.interfaces;


import project.com.instahelp.utils.User;

public interface LocalDataManager {
    User getCurrentUser();
    void updateUser(User user);
    void updateUserName(String newName);
    void updatePhoneNumber(String newNumber);
}
