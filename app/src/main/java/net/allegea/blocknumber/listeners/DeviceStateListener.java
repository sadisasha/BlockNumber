package net.allegea.blocknumber.listeners;

import android.content.Context;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

import net.allegea.blocknumber.activities.BlacklistViewActivity;
import net.allegea.blocknumber.objects.BlockedContact;
import net.allegea.blocknumber.services.BlockNumberService;

import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DeviceStateListener extends PhoneStateListener {
    private ITelephony telephonyService;

    private Context context;

    public DeviceStateListener(Context context) {
        this.context = context;
        initializeTelephonyService();
    }

    private void initializeTelephonyService() {
        try {
            TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            Class aClass = Class.forName(telephonyManager.getClass().getName());
            Method method = aClass.getDeclaredMethod("getITelephony");
            method.setAccessible(true);
            telephonyService = (ITelephony)method.invoke(telephonyManager);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCallStateChanged(int state, final String incomingNumber) {
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                final BlockedContact contact = BlacklistViewActivity.blackList.get(incomingNumber);
                if (contact != null && contact.isBlockedForCalling()) {
                    try {
                        telephonyService.endCall();
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss");
                                Date date = new Date();
                                String currentDate = dateFormat.format(date);
                                String message = "Call Blocked;;A call from " + contact.getName() + " (" + incomingNumber + ") was blocked at " + currentDate + ";;" + contact.getName() + ";;" + incomingNumber + ";;" + currentDate + ";;NULL;;\r\n";
                                writeInLog(message);
                            }
                        });
                        t.start();
                    }
                    catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
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