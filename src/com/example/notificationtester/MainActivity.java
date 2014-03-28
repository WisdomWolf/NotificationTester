package com.example.notificationtester;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView txtView;
    private NotificationReceiver nReceiver;
    private TextView buildText;
    private int mId = 1337;
    private String TAG = this.getClass().getSimpleName();
    private TextToSpeech mTTS;
	private ZipFile zf;
	private int msgCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtView = (TextView) findViewById(R.id.textView);
        nReceiver = new NotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.notificationlistener.NOTIFICATION_LISTENER_EXAMPLE");
        registerReceiver(nReceiver,filter);
		buildText = (TextView) findViewById(R.id.txtViewB);
		getBuildTime();
		Button btnList = (Button) findViewById(R.id.btnListNotify);
		btnList.setText("Clear");
		
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nReceiver);
    }



    public void buttonClicked(View v){

        if(v.getId() == R.id.btnCreateNotify){
            NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationCompat.Builder ncomp = new NotificationCompat.Builder(this)
            .setContentTitle("WisdomWorks")
            .setContentText("Notification Listener Service Example")
            .setTicker("Wisdom is wise, but this is crazy.")
            .setSmallIcon(R.drawable.ic_launcher)
            .setAutoCancel(false); //want to leave it in status bar for easy way back
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            Intent resultIntent = new Intent(this, MainActivity.class);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = 
            		stackBuilder.getPendingIntent(
            				0,
            				PendingIntent.FLAG_UPDATE_CURRENT
            		);
            ncomp.setContentIntent(resultPendingIntent);
            nManager.notify(mId,ncomp.build());
        }
        else if(v.getId() == R.id.btnListNotify){
//            Intent i = new Intent("com.example.notificationlistener.NOTIFICATION_LISTENER_SERVICE_EXAMPLE");
//            i.putExtra("command","list");
//            sendBroadcast(i);
        	//temporarily repurposing button or so I hope
        	txtView.setText("");
        }


    }
    
    //will bring them to appropriate settings menu to enable our service
    public void goSettings(View v){
    	Intent intent;
    	if (Build.VERSION.SDK_INT < 18){
    		startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    	} else {
    		intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
    		startActivity(intent);
    	}
    }
	
    //used to get and set the text at bottom for easy revision identification
	private void getBuildTime(){
		try{
			ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), 0);
			zf = new ZipFile(ai.sourceDir);
			ZipEntry ze = zf.getEntry("classes.dex");
			long time = ze.getTime();
			String s = SimpleDateFormat.getInstance().format(new java.util.Date(time));
			buildText.setText(s);
		}catch(Exception e){
			buildText.setText("error");
		}
	}

    class NotificationReceiver extends BroadcastReceiver{
		@Override
        public void onReceive(Context context, Intent intent) {
        	Log.d(TAG,"Broadcast received in Main Activity from " + intent.getStringExtra("broadcasting_method"));
        	String eventText = "";
        	String notificationText = "";
        	String tickerText = "";
        	String titleText = "";
        	msgCount++;
        	//variables necessary for writing the same text that gets sent to txtView to a file
//        	String filename = "notificationOutput" + msgCount;
        	
        	//Using a buffered writer with append for cleaner implementation
        	String filename = "notificationOutput.txt";
        	BufferedWriter buf;
//        	FileOutputStream outputStream;
        	
        	if (intent.getStringExtra("notification_event") != null){
        		Log.d(TAG,"*******Received notification_event " + eventText);
        		eventText = intent.getStringExtra("notification_event");
        		String temp = eventText + txtView.getText();
        		txtView.setText(temp);;
        	} else {
            	if (intent.getStringExtra("notification_title") != null){
            		titleText = "Title: " 
            				+ intent.getStringExtra("notification_title") + "\n";
            	}
    			if (intent.getParcelableExtra("statusbar_notification_object") != null){
    				Log.d(TAG,"***parcelable received");
    				Parcelable parcel = intent.getParcelableExtra("statusbar_notification_object");
    				Notification noti = (Notification) parcel;
    				if (noti.tickerText != null){
    					tickerText = "Ticker Text: " + noti.tickerText.toString() + "\n";
    				}
    				if (parcel instanceof Notification) {
    					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
    						notificationText += "Notification Text: "
    								+ getExtraBigData((Notification) parcel, notificationText.trim());
    					} else {
    						notificationText += "Notification Text: "
    								+ getExtraData((Notification) parcel, notificationText.trim());
    					}
    				}
    			}
    			String temp = titleText
    					+ tickerText
    					+ notificationText + "\n";
    			if (temp == null || temp.equals("") || temp.equals("null")){
    				Log.d(TAG,"notificationText empty");
    			} else {
    				txtView.setText(temp);
    				//write temp to a text file for accurate record to base pattern parses off of
    				try {
//    					outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
//    					outputStream.write(temp.getBytes());
//    					outputStream.close();
    					buf = new BufferedWriter(new FileWriter(filename, true));
    					buf.append(temp);
    					buf.close();
    				} catch (Exception e) {
    					e.printStackTrace();
    				}
    			}
        	}
        	
			//mTTS.speak(temp, TextToSpeech.QUEUE_FLUSH, null);
		
        }
		
		public void parseNotification (Notification noti, String pkgLabel){
			//do stuff
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
    }



}
