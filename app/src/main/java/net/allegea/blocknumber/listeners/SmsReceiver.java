package net.allegea.blocknumber.listeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import net.allegea.blocknumber.activities.BlacklistViewActivity;
import net.allegea.blocknumber.objects.BlockedContact;
import net.allegea.blocknumber.services.BlockNumberService;

import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SmsReceiver extends BroadcastReceiver {
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        blockSms(intent);
    }

    public void blockSms(Intent intent) {
        Bundle bundle = intent.getExtras();
        final SmsMessage[] messages;
        if (bundle != null) {
            Object[] pdus = (Object[])bundle.get("pdus");
            messages = new SmsMessage[pdus.length];
            for (int i = 0; i < messages.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
            }
            final BlockedContact contact = BlacklistViewActivity.blackList.get(messages[0].getOriginatingAddress());
            if (contact != null && contact.isBlockedForMessages()) {
                abortBroadcast();
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss");
                        Date date = new Date();
                        String currentDate = dateFormat.format(date);
                        String message = "Message Blocked;;A message from " + contact.getName() + " (" + messages[0].getOriginatingAddress() + ") was blocked at " + currentDate + ";;" + contact.getName() + ";;" + messages[0].getOriginatingAddress() + ";;" + currentDate + ";;" + messages[0].getMessageBody() + ";;\r\n";
                        writeInLog(message);
                    }
                });
                t.start();
            }
        }
    }

    public void writeInLog(String message) {
        try {
            OutputStreamWriter osw = new OutputStreamWriter(context.openFileOutput("CallLog.txt", Context.MODE_APPEND));
            osw.append(message);
            osw.close();
            System.out.println("Writed in log succesfully");
        }
        catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
    }
}
