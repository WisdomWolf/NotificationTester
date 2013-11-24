package com.example.notificationtester;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Parcelable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

@TargetApi(18)
public class NLService extends NotificationListenerService {

    private String TAG = this.getClass().getSimpleName();
    private NLServiceReceiver nlservicereciver;
    private Handler mHandler;
    @Override
    public void onCreate() {
    	mHandler = new Handler();
        super.onCreate();
        nlservicereciver = new NLServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.notificationlistener.NOTIFICATION_LISTENER_SERVICE_EXAMPLE");
        registerReceiver(nlservicereciver,filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nlservicereciver);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
    	
    	Looper.prepare();
        Log.i(TAG,"**********  onNotificationPosted");
        Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
        Log.i(TAG,"Notification String:" + sbn.toString());
        //Log.i(TAG,"Intent:" + " " + sbn.getNotification().contentIntent.toString());
        Intent i = new  Intent("com.example.notificationlistener.NOTIFICATION_LISTENER_EXAMPLE");
        i.putExtra("notification_event","onNotificationPosted :" + sbn.getPackageName() + "\n");
        //additional debugging
        sendBroadcast(i);
        notificationCapture(sbn);
        Looper.loop();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG,"********** onNotificationRemoved");
        Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText +"\t" + sbn.getPackageName());
        Intent i = new  Intent("com.example.notificationlistener.NOTIFICATION_LISTENER_EXAMPLE");
        i.putExtra("notification_event","onNotificationRemoved :" + sbn.getPackageName() + "\n");
        sendBroadcast(i);
        
    }
    
    public void notificationCapture(StatusBarNotification sbn){
    	Log.i(TAG,"********* notificationCapture");
    	Intent i = new  Intent("com.example.notificationlistener.NOTIFICATION_LISTENER_EXAMPLE");
    	String notificationText = sbn.getNotification().tickerText.toString();
    	Parcelable parcelable = sbn.getNotification();
    	notificationText += "\n" + getExtraBigData((Notification) parcelable, notificationText.trim());
    	i.putExtra("notification_event"," " + sbn.getPackageName() + "\n" 
    			+ notificationText + "\n");
    	sendBroadcast(i);
   }
    private String getExtraData(Notification notification, String existing_text) {
    	Log.i(TAG,"******** getExtraData");
        RemoteViews views = notification.contentView;
        if (views == null) {
            return "";
        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        try {
            ViewGroup localView = (ViewGroup) inflater.inflate(views.getLayoutId(), null);
            views.reapply(getApplicationContext(), localView);
            return dumpViewGroup(0, localView, existing_text);
        } catch (android.content.res.Resources.NotFoundException e) {
            return "";
        } catch (RemoteViews.ActionException e) {
            return "";
        }
    }
    
    @TargetApi(18)
    private String getExtraBigData(Notification notification, String existing_text) {
    	Log.i(TAG,"******** getExtraBigData");
        RemoteViews views = null;
        try {
            views = notification.bigContentView;
        } catch (NoSuchFieldError e) {
            return getExtraData(notification, existing_text);
        }
        if (views == null) {
            return getExtraData(notification, existing_text);
        }
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        try {
            ViewGroup localView = (ViewGroup) inflater.inflate(views.getLayoutId(), null);
            views.reapply(getApplicationContext(), localView);
            return dumpViewGroup(0, localView, existing_text);
        } catch (android.content.res.Resources.NotFoundException e) {
            return "";
        }
    }
    
    private String dumpViewGroup(int depth, ViewGroup vg, String existing_text) {
        String text = "";
        for (int i = 0; i < vg.getChildCount(); ++i) {
            View v = vg.getChildAt(i);
            if (v.getId() == android.R.id.title || v instanceof android.widget.Button
                    || v.getClass().toString().contains("android.widget.DateTimeView")) {
                if (existing_text.isEmpty() && v.getId() == android.R.id.title) {
                } else {
                    continue;
                }
            }

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                if (tv.getText().toString() == "..."
                        || isInteger(tv.getText().toString())
                        || tv.getText().toString().trim().equalsIgnoreCase(existing_text)) {
                    continue;
                }
                text += tv.getText().toString() + "\n";
            }
            if (v instanceof ViewGroup) {
                text += dumpViewGroup(depth + 1, (ViewGroup) v, existing_text);
            }
        }
        return text;
    }
    
    public boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    

    class NLServiceReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra("command").equals("clearall")){
                    NLService.this.cancelAllNotifications();
            }
            else if(intent.getStringExtra("command").equals("list")){
                Intent i1 = new  Intent("com.example.notificationlistener.NOTIFICATION_LISTENER_EXAMPLE");
                i1.putExtra("notification_event","=====================");
                sendBroadcast(i1);
                for (StatusBarNotification sbn : NLService.this.getActiveNotifications()) {
                   notificationCapture(sbn);
                }
                Intent i2 = new  Intent("com.example.notificationlistener.NOTIFICATION_LISTENER_EXAMPLE");
                i2.putExtra("notification_event","===== Notification List ====");
                sendBroadcast(i2);

            }

        }
    }

}