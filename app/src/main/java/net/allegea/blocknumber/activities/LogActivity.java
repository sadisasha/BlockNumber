package net.allegea.blocknumber.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;
import android.widget.TabHost;

import net.allegea.blocknumber.R;
import net.allegea.blocknumber.objects.BlockedActivity;
import net.allegea.blocknumber.objects.BlockedCall;
import net.allegea.blocknumber.objects.BlockedSms;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class LogActivity extends Activity {
    private static ArrayList<BlockedActivity> logList;

    private static ListView logListViewAll;

    private static ListView logListViewCalls;

    private static ListView logListViewSms;

    private static Activity instance;

    private TabHost tabHost;

    private Resources resources;

    public static void deleteFromLog(BlockedActivity toDelete, int pos) {
        logList.remove(toDelete);
        logListViewAll.setAdapter(new LogAdapter(instance, logList));
        ArrayList<BlockedActivity> callsList = new ArrayList<BlockedActivity>();
        for (BlockedActivity blockac : logList) {
            if (blockac.getTitle().startsWith("Call")) {
                callsList.add(blockac);
            }
        }
        logListViewCalls.setAdapter(new LogAdapter(instance, callsList));
        ArrayList<BlockedActivity> smsList = new ArrayList<BlockedActivity>();
        for (BlockedActivity blockac : logList) {
            if (blockac.getTitle().startsWith("Message")) {
                smsList.add(blockac);
            }
        }
        logListViewSms.setAdapter(new LogAdapter(instance, smsList));
    }

    public static void call(String number) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        instance.startActivity(callIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(BlacklistViewActivity.theme);
        resources = getResources();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        tabHost = (TabHost)findViewById(android.R.id.tabhost);
        tabHost.setup();
        TabHost.TabSpec tabSpec = tabHost.newTabSpec("Calls/Sms");
        tabSpec.setContent(R.id.tab1);
        tabSpec.setIndicator("Calls/Sms", resources.getDrawable(R.drawable.allblue));
        tabHost.addTab(tabSpec);
        TabHost.TabSpec spec2 = tabHost.newTabSpec("Calls");
        spec2.setContent(R.id.tab2);
        spec2.setIndicator("Calls", resources.getDrawable(R.drawable.phoneblue));
        tabHost.addTab(spec2);
        TabHost.TabSpec spec3 = tabHost.newTabSpec("Sms");
        spec3.setContent(R.id.tab3);
        spec3.setIndicator("Sms", resources.getDrawable(R.drawable.smsblue));
        tabHost.addTab(spec3);
        tabHost.setCurrentTab(0);
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
            }
        });
        instance = this;
        loadLog();
        int[] colors = {0, 0xFF00FFFF, 0};
        logListViewAll = (ListView)findViewById(R.id.logListViewAll);
        logListViewAll.setDivider(new GradientDrawable(Orientation.RIGHT_LEFT, colors));
        logListViewAll.setDividerHeight(3);
        logListViewAll.setAdapter(new LogAdapter(this, logList));
        logListViewCalls = (ListView)findViewById(R.id.logListViewCalls);
        logListViewCalls.setDivider(new GradientDrawable(Orientation.RIGHT_LEFT, colors));
        logListViewCalls.setDividerHeight(3);
        ArrayList<BlockedActivity> callsList = new ArrayList<BlockedActivity>();
        for (BlockedActivity blockac : logList) {
            if (blockac.getTitle().startsWith("Call")) {
                callsList.add(blockac);
            }
        }
        logListViewCalls.setAdapter(new LogAdapter(instance, callsList));
        logListViewSms = (ListView)findViewById(R.id.logListViewSms);
        logListViewSms.setDivider(new GradientDrawable(Orientation.RIGHT_LEFT, colors));
        logListViewSms.setDividerHeight(3);
        ArrayList<BlockedActivity> smsList = new ArrayList<BlockedActivity>();
        for (BlockedActivity blockac : logList) {
            if (blockac.getTitle().startsWith("Message")) {
                smsList.add(blockac);
            }
        }
        logListViewSms.setAdapter(new LogAdapter(instance, smsList));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    private void loadLog() {
        try {
            logList = new ArrayList<BlockedActivity>();
            InputStreamReader isr = new InputStreamReader(openFileInput("CallLog.txt"));
            BufferedReader br = new BufferedReader(isr);
            String line = br.readLine();
            while (line != null) {
                String[] textParts = line.split(";;");
                String title = textParts[0];
                String message = textParts[1];
                String name = textParts[2];
                String number = textParts[3];
                String hour = textParts[4];
                String bodyMessage = textParts[5];
                BlockedActivity activity;
                if (title.startsWith("Message")) {
                    activity = new BlockedSms(title, message, name, number, hour, bodyMessage);
                }
                else {
                    activity = new BlockedCall(title, message, name, number, hour);
                }
                logList.add(activity);
                line = br.readLine();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.e("Error", e.getMessage());
        }
    }
}
