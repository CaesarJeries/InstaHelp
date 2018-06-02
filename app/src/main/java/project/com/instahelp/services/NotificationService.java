package project.com.instahelp.services;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.List;
import java.util.Map;

import project.com.instahelp.R;
import project.com.instahelp.utils.Defaults;
import project.com.instahelp.utils.Post;
import project.com.instahelp.utils.ServerComm;

public class NotificationService extends Service {

    int notificationsCounter = 0;
    int messagesCounter = 0;

    enum Type{
        Notification,
        Message,
        NewPost
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //When the service is started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service", "service started");
        int retval =  super.onStartCommand(intent, flags, startId);
        final String user_id = getSharedPreferences("current_user", MODE_PRIVATE).getString("current_id", null);
        Firebase.setAndroidContext(getApplicationContext());
        Firebase ref = new Firebase(Defaults.FIREBASE_URL);
        ref.child(ServerComm.NOTIFICATIONS_CHILD).child(user_id).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                notificationsCounter++;
                showNotification(Type.Notification);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                notificationsCounter--;
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        ref.child(ServerComm.MESSAGES_CHILD).child(user_id).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                messagesCounter++;
                showNotification(Type.Message);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                messagesCounter--;
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        ref.child(ServerComm.POSTS_CHILD).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Post p = new Post((Map<String, Object>) dataSnapshot.getValue());
                if(!p.getUserId().equals(user_id)){
                    showNotification(Type.NewPost);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        return retval;
    }

    // restart the service when app is killed.
    @Override
    public void onTaskRemoved(Intent rootIntent){
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent =  PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);

        super.onTaskRemoved(rootIntent);
    }

    private void showNotification(Type type){
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> services = activityManager
                .getRunningTasks(Integer.MAX_VALUE);
        boolean isActivityFound = false;

        if (services.get(0)
                .topActivity.getPackageName()
                .equalsIgnoreCase(getApplicationContext().getPackageName())) {
            isActivityFound = true;
        }


        if(!isActivityFound){
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setSmallIcon(R.mipmap.ic_stat_toggle_star);
            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_stat_toggle_star));
            builder.setContentTitle("InstaHelp");
            String msg = null;
            switch(type){
                case Notification:{
                    msg = notificationsCounter + " new notification";
                    if(notificationsCounter > 1) msg += "s";
                    break;
                }
                case Message:{
                    msg = messagesCounter + " new message";
                    if(messagesCounter > 1) msg += "s";
                    break;
                }
                case NewPost:{
                    msg = "New posts are available";
                }
            }
            builder.setContentText(msg);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(1, builder.build());
        }
    }
}