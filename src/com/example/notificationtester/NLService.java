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

    @Override
    public void onCreate() {
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
        Log.i(TAG,"**********  onNotificationPosted");
        Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
        Log.i(TAG,"Notification String:" + sbn.toString());
        //Log.i(TAG,"Intent:" + " " + sbn.getNotification().contentIntent.toString());
        Intent i = new  Intent("com.example.notificationlistener.NOTIFICATION_LISTENER_EXAMPLE");
        i.putExtra("notification_event","onNotificationPosted :" + sbn.getPackageName() + "\n");
        //additional debugging
        sendBroadcast(i);
        notificationCapture(sbn);
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
		if (notificationText == null) {
			notificationText = "";
		}
		Parcelable parcelable = sbn.getNotification();
    	i.putExtra("statusbar_notification_object",parcelable);
		i.putExtra("notification_text",notificationText);
    	sendBroadcast(i);
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
