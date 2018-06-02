package project.com.instahelp.interfaces;


public interface BadgeManager {
    enum Tab{
        CHAT, NOTIFICATIONS;

        public int toInt(){
            if(this == CHAT) return 4;
            return 3;
        }
    }

    void increment(Tab tab);
    void hide(Tab tab);

}
