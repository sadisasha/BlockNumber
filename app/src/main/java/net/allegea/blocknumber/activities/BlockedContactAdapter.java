package net.allegea.blocknumber.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import net.allegea.blocknumber.R;
import net.allegea.blocknumber.notifications.ToastNotification;
import net.allegea.blocknumber.objects.BlockedContact;

import java.util.ArrayList;

public class BlockedContactAdapter extends BaseAdapter {
    private BlacklistViewActivity activity;

    public BlockedContactAdapter(BlacklistViewActivity activity) {
        this.activity = activity;
    }

    @Override
    public int getCount() {
        ArrayList<BlockedContact> contacts = new ArrayList<BlockedContact>();
        contacts.addAll(activity.blackList.values());
        return contacts.size();
    }

    @Override
    public Object getItem(int position) {
        ArrayList<BlockedContact> contacts = new ArrayList<BlockedContact>();
        contacts.addAll(activity.blackList.values());
        return contacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.blacklist_layout, null);
        }
        ArrayList<BlockedContact> contacts = new ArrayList<BlockedContact>();
        contacts.addAll(activity.blackList.values());
        final BlockedContact contact = contacts.get(position);
        TextView name = (TextView)view.findViewById(R.id.blacklistNameTxt);
        TextView number = (TextView)view.findViewById(R.id.blacklistNumberTxt);
        name.setText(contact.getName());
        number.setText(contact.getNumber());
        final ImageButton callButton = (ImageButton)view.findViewById(R.id.blacklistCallButton);
        final ImageButton smsButton = (ImageButton)view.findViewById(R.id.blacklistSmsButton);
        final ImageButton deleteButton = (ImageButton)view.findViewById(R.id.blacklistDeleteButton);
        if (contact.isBlockedForCalling()) {
            callButton.setImageResource(R.drawable.phonered);
        }
        if (contact.isBlockedForMessages()) {
            smsButton.setImageResource(R.drawable.smsred);
        }
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contact.isBlockedForCalling()) {
                    contact.setBlockedForCalling(false);
                    callButton.setImageResource(R.drawable.phoneblack);
                    ToastNotification.showDefaultShortNotification(contact.getName() + " can call you");
                }
                else {
                    contact.setBlockedForCalling(true);
                    callButton.setImageResource(R.drawable.phonered);
                    ToastNotification.showDefaultShortNotification(contact.getName() + " can not call you");
                }
            }
        });
        smsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contact.isBlockedForMessages()) {
                    contact.setBlockedForMessages(false);
                    smsButton.setImageResource(R.drawable.smsblack);
                    ToastNotification.showDefaultShortNotification(contact.getName() + " can text you");
                }
                else {
                    contact.setBlockedForMessages(true);
                    smsButton.setImageResource(R.drawable.smsred);
                    ToastNotification.showDefaultShortNotification(contact.getName() + " can not text you");
                }
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(v.getContext()).create();
                dialog.setTitle("Delete contact");
                dialog.setMessage("Are you sure you want to delete this contact from blacklist?");
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.blackList.remove(contact.getNumber());
                        notifyDataSetChanged();
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
        });
        return view;
    }
}
