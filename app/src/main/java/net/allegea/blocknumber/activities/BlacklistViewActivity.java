package net.allegea.blocknumber.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import net.allegea.blocknumber.R;
import net.allegea.blocknumber.notifications.ToastNotification;
import net.allegea.blocknumber.objects.BlockedContact;
import net.allegea.blocknumber.services.BlockNumberService;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class BlacklistViewActivity extends Activity implements Serializable {
    private static final long serialVersionUID = -1875124806221731429L;

    public static ActivityManager manager;

    public static Context applicationContext;

    public static int theme;

    private static ListView view;

    private static BlockedContactAdapter adapter;

    public static HashMap<String, BlockedContact> blackList;

    private Button buttonAdd;

    private Button buttonStartService;

    private Button buttonStopService;

    private Button buttonLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        theme = Integer.parseInt(prefs.getString("theme_preference", "16974143"));
        setTheme(theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blacklist_view);
        applicationContext = getApplicationContext();
        manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        view = (ListView)findViewById(R.id.blacklistListView);
        int[] colors = {0, 0xFF00FFFF, 0};
        view.setDivider(new GradientDrawable(Orientation.RIGHT_LEFT, colors));
        view.setDividerHeight(3);
        adapter = new BlockedContactAdapter(this);
        loadData();
        view.setAdapter(adapter);
        buttonStartService = (Button)findViewById(R.id.buttonStart);
        buttonStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService();
            }
        });
        buttonStopService = (Button)findViewById(R.id.buttonStop);
        buttonStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService();
            }
        });
        buttonLog = (Button)findViewById(R.id.buttonLog);
        buttonLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog(view);
            }
        });
        buttonAdd = (Button)findViewById(R.id.buttonAdd);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectContactFromDevice();
            }
        });
    }

    @Override
    protected void onDestroy() {
        saveData();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                Cursor cursor = null;
                try {
                    cursor = getContentResolver().query(uri, new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE}, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        String name = cursor.getString(0);
                        String number = cursor.getString(1).replaceAll("[ ( | ) | \\- ]", "");
                        int type = cursor.getInt(2);
                        BlockedContact contact = new BlockedContact(name, number, type, true, true);
                        addContactToBlackList(contact);
                    }
                }
                finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
    }

    public void addContactToBlackList(BlockedContact contact) {
        blackList.put(contact.getNumber(), contact);
        adapter.notifyDataSetChanged();
        ToastNotification.showDefaultShortNotification("Contact added to blacklist successfully");
    }

    public void saveData() {
        try {
            FileOutputStream fos = openFileOutput("BlockNumber.data", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(blackList);
            fos.close();
            oos.close();
            ToastNotification.showDefaultShortNotification("Saving Data...");
        }
        catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
    }

    public void loadData() {
        try {
            FileInputStream fis = openFileInput("BlockNumber.data");
            ObjectInputStream ois = new ObjectInputStream(fis);
            blackList = (HashMap<String, BlockedContact>)ois.readObject();
            fis.close();
            ois.close();
        }
        catch (Exception e) {
            blackList = new HashMap<String, BlockedContact>();
            Log.e("Error", e.getMessage());
        }
    }

    public void startService() {
        if (!checkIfServiceIsAlreadyRunning()) {
            if (blackList.size() > 0) {
                Intent i = new Intent(this, BlockNumberService.class);
                startService(i);
            }
            else {
                ToastNotification.showDefaultShortNotification("Blacklist is empty, service can't start");
            }
        }
        else {
            ToastNotification.showDefaultShortNotification("Service is running already");
        }
    }

    public void stopService() {
        if (checkIfServiceIsAlreadyRunning()) {
            stopService(new Intent(this, BlockNumberService.class));
        }
    }

    public void showLog(View view) {
        if (checkIfServiceIsAlreadyRunning()) {
            AlertDialog dialog = new AlertDialog.Builder(view.getContext()).create();
            dialog.setTitle("My log");
            dialog.setMessage("Right now service is running so the application will stop it, do you want to continue? You can start again the service from the menu");
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    stopService();
                    Intent i = new Intent(BlacklistViewActivity.this, LogActivity.class);
                    startActivity(i);
                }
            });
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            dialog.setCancelable(false);
            dialog.setIcon(R.drawable.advert);
            dialog.show();
        }
        else {
            Intent i = new Intent(this, LogActivity.class);
            startActivity(i);
        }
    }

    public void selectContactFromDevice() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        startActivityForResult(intent, 1);
    }

    public static boolean checkIfServiceIsAlreadyRunning() {
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (BlockNumberService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
