package net.allegea.blocknumber.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import net.allegea.blocknumber.R;
import net.allegea.blocknumber.activities.BlacklistViewActivity;
import net.allegea.blocknumber.listeners.ServiceReceiver;
import net.allegea.blocknumber.listeners.SmsReceiver;
import net.allegea.blocknumber.notifications.ToastNotification;
import net.allegea.blocknumber.objects.BlockedContact;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;

public class BlockNumberService extends Service {
    public static final int NOTIFICATION_ID = 111;

    private static ServiceReceiver service;

    private static SmsReceiver sms;

    @Override
    public void onCreate() {
        service = new ServiceReceiver(getApplicationContext());
        sms = new SmsReceiver();
        registerReceiver(service, new IntentFilter("android.intent.action.PHONE_STATE"));
        registerReceiver(sms, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
        showStatusBarNotification("Block number is running now");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        ToastNotification.showDefaultShortNotification("Turning block number off");
        service.stopListening();
        unregisterReceiver(service);
        unregisterReceiver(sms);
        service = null;
        sms = null;
        cancelStatusBarNotification();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void cancelStatusBarNotification() {
        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
    }

    public void showStatusBarNotification(String message) {
        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(BlockNumberService.this, BlacklistViewActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(this).setContentTitle("Block number notification").setWhen(System.currentTimeMillis()).setContentText(message).setSmallIcon(R.drawable.running_not_icon).setContentIntent(pendingIntent).build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        manager.notify(NOTIFICATION_ID, notification);
    }
}
