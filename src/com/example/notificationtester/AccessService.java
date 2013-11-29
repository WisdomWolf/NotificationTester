package com.example.notificationtester;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Handler;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RemoteViews;
import android.widget.TextView;


public class AccessService extends AccessibilityService {
	private Handler mHandler;
	
	public void onAccessibilityEvent(AccessibilityEvent event) {
		PackageManager pm = getPackageManager();
		String eventPackageName;
        if (event.getPackageName() != null){
            eventPackageName = event.getPackageName().toString();
        } else {
            eventPackageName = "";
        }
        // get the title
        String title = "";
        try {
            title = pm.getApplicationLabel(pm.getApplicationInfo(eventPackageName, 0)).toString();
        } catch (NameNotFoundException e) {
            title = eventPackageName;
        }
        // get the notification text
        String notificationText = event.getText().toString();
        // strip the first and last characters which are [ and ]
        notificationText = notificationText.substring(1, notificationText.length() - 1);
        
        Parcelable parcel = event.getParcelableData();
        
        Intent i = new  Intent("com.example.notificationlistener.NOTIFICATION_LISTENER_EXAMPLE");
        i.putExtra("statusbar_notification_object", parcel);
		i.putExtra("notification_event",notificationText);
        sendBroadcast(i);
            
	}
	
    @Override
    public void onInterrupt() {

    }
	private String getExtraData(Notification notification, String existing_text) {
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
	
	 @Override
	    protected void onServiceConnected() {
	        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
	        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_VISUAL;
	        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
	        info.notificationTimeout = 100;
	        setServiceInfo(info);

	        mHandler = new Handler();
	    }
    
    @TargetApi(18)
    private String getExtraBigData(Notification notification, String existing_text) {
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
}
