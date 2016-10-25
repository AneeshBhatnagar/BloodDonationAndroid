package com.aneesh.blooddonation;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.aneesh.blooddonation.app.AppConfig;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GCMNotificationIntentService extends IntentService {
    // Sets an ID for the notification, so it can be updated
    public static final int notifyID = 9001;
    NotificationCompat.Builder builder;

    public GCMNotificationIntentService() {
        super("GcmIntentService");
    }

    public static final String TAG = "GCMNotificationIntentService";

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
                    .equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
                    .equals(messageType)) {
                sendNotification("Deleted messages on server: "
                        + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
                    .equals(messageType)) {

                sendNotification(""    + extras.get(AppConfig.MSG_KEY)); //When Message is received normally from GCM Cloud Server
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msgs) {
        Intent resultIntent = new Intent(this, NotificationReceived.class);
        resultIntent.putExtra("jsonMessage", msgs);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
                resultIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder mNotifyBuilder;
        NotificationManager mNotificationManager;

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotifyBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Blood Required")
                .setContentText("There is urgent need of blood.")
                .setSmallIcon(R.mipmap.ic_launcher);
        // Set pending intent
        mNotifyBuilder.setContentIntent(resultPendingIntent);

        // Set Vibrate, Sound and Light
        int defaults = 0;
        defaults = defaults | Notification.DEFAULT_LIGHTS;
        defaults = defaults | Notification.DEFAULT_VIBRATE;
        defaults = defaults | Notification.DEFAULT_SOUND;

        mNotifyBuilder.setDefaults(defaults);
        // Set the content for Notification
        mNotifyBuilder.setContentText("Blood Request Notification");
        // Set autocancel
        mNotifyBuilder.setAutoCancel(true);
        // Post a notification
        mNotificationManager.notify(notifyID, mNotifyBuilder.build());
    }
}