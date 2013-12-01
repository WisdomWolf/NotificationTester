package com.example.notificationtester;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Parcelable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

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
        //Intent i = new  Intent("com.example.notificationlistener.NOTIFICATION_LISTENER_EXAMPLE");
        //i.putExtra("notification_event","onNotificationPosted :" + sbn.getPackageName() + "\n");
        //additional debugging
       // sendBroadcast(i);
        notificationCapture(sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        //most of what happens here is irrelevant
    	Log.i(TAG,"********** onNotificationRemoved");
        Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText +"\t" + sbn.getPackageName());
        Intent i = new  Intent("com.example.notificationlistener.NOTIFICATION_LISTENER_EXAMPLE");
        i.putExtra("notification_event","onNotificationRemoved :" + sbn.getPackageName() + "\n");
        sendBroadcast(i);
        
    }
    
    public void notificationCapture(StatusBarNotification sbn){
    	Log.i(TAG,"********* notificationCapture");
    	Intent i = new  Intent("com.example.notificationlistener.NOTIFICATION_LISTENER_EXAMPLE");
    	//String tickerText = sbn.getNotification().tickerText.toString();
		//if (tickerText == null || tickerText.equalsIgnoreCase("null")) {
		//	tickerText = "";
		//}
		Parcelable parcelable = sbn.getNotification();
		if (parcelable == null){
			return;
		}
    	i.putExtra("statusbar_notification_object",parcelable);
    	PackageManager pm = getPackageManager();
		String PackageName;
        if (sbn.getPackageName() != null){
            PackageName = sbn.getPackageName().toString();
        } else {
            PackageName = "";
        }
        // get the title
        String title = "";
        try {
            title = pm.getApplicationLabel(pm.getApplicationInfo(PackageName, 0)).toString();
        } catch (NameNotFoundException e) {
            title = PackageName;
        }
        i.putExtra("notification_title", title);
		//i.putExtra("ticker_text",tickerText);
    	sendBroadcast(i);
   }
    
    

    class NLServiceReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
        	if(intent.hasExtra("command")){
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

}
