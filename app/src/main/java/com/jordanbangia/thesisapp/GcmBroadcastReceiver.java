package com.jordanbangia.thesisapp;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Patterns;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Created by Jordan on 12/30/2014.
 */
public class GcmBroadcastReceiver extends BroadcastReceiver {
    NotificationManager mNotificationManager;
    public static final int NOTIFICATION_ID = 1;

    private static final String EMAIL = "email";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        String messageType = gcm.getMessageType(intent);

        if(!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString(), context);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " + extras.toString(), context);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                sendNotification("Received: " + extras.toString(), context);
                handleNewMessage(extras, context);
            }
        }

    }


    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg, Context context) {
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("GCM Notification")
            .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
            .setContentText(msg);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private void handleNewMessage(final Bundle extras, final Context context) {
        String instruction = extras.getString("instr");

        String tmp = "";
        if (EMAIL.equalsIgnoreCase(instruction)) {
            //trying to access user email
            Pattern emailPattern = Patterns.EMAIL_ADDRESS;
            Account[] accounts = AccountManager.get(context).getAccounts();
            for (Account account: accounts) {
                if (emailPattern.matcher(account.name).matches()) {
                    tmp = account.name;
                }
            }
        }

        if (tmp.equalsIgnoreCase(""))
            return;

        final String ret = tmp;
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                String msg = "";
                try {
                    Bundle data = new Bundle();
                    data.putString("response", ret);
                    data.putString("action", "com.grokkingandroid.sampleapp.samples.gcm.RESPONSE");
                    data.putString("requestMsgId", extras.getString("from"));
                    String id = Integer.toString(MainActivity.msgId.incrementAndGet());
                    GoogleCloudMessaging.getInstance(context).send(MainActivity.SENDER_ID + "@gcm.googleapis.com", id, data);
                } catch (IOException e) {
                    msg = "Error : " + e.getMessage();
                }
                return msg;
            }
        }.execute();

    }
}
