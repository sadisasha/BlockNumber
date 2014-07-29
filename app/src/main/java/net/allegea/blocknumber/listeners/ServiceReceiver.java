package net.allegea.blocknumber.listeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class ServiceReceiver extends BroadcastReceiver {
    private static TelephonyManager telephony;

    private static DeviceStateListener phoneListener;

    public ServiceReceiver(Context context) {
        telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        phoneListener = new DeviceStateListener(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    public void stopListening() {
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_NONE);
    }
}
