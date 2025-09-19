package com.example.citylinkrentals.network;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.citylinkrentals.Activities.MainActivity;
import com.example.citylinkrentals.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // Check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            handleNow(remoteMessage.getData());
        }
        
        if (remoteMessage.getNotification() != null) {
            sendNotification(remoteMessage.getNotification().getBody());
        }
    }
    
    private void handleNow(Map<String, String> data) {

        String messageContent = data.get("message");
        String senderId = data.get("senderId");
        
        sendNotification(messageContent);
        
        Intent intent = new Intent("NEW_MESSAGE");
        intent.putExtra("message", messageContent);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder notificationBuilder =
            new NotificationCompat.Builder(this, "channel_id")
                .setSmallIcon(R.drawable.notifications_24px)
                .setContentTitle("New Message")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }
}