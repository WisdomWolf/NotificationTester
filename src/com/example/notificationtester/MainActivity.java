package com.example.notificationtester;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

public class MainActivity extends Activity implements TextToSpeech.OnInitListener{

	private TextView txtView;
    private NotificationReceiver nReceiver;
    private TextView buildText;
    private int mId = 1337;
    private String TAG = this.getClass().getSimpleName();
	private ZipFile zf;
	private TextToSpeech mTTS;
	private String speakableText = "";
	private String lastMessage = "";
	Pattern firstHangoutsSender = Pattern.compile("(?<=:\\s).*?(?=,)");
	

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
		mTTS = new TextToSpeech(this,this);
		Intent i = new Intent("com.example.notificationlistener.NOTIFICATION_LISTENER_SERVICE_EXAMPLE");
		i.putExtra("command","cancel");
		sendBroadcast(i);
		
    }

    @Override
    protected void onDestroy() {
    	if (mTTS != null){
    		mTTS.stop();
    		mTTS.shutdown();
    	}
    	super.onDestroy();
        unregisterReceiver(nReceiver);
    }
    
    @Override
    public void onInit(int status) {

		if (status == TextToSpeech.SUCCESS) {

			int result = mTTS.setLanguage(Locale.US);

			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("TTS", "This Language is not supported");
			} else {
				speakOut();
			}

		} else {
			Log.e("TTS", "Initilization Failed!");
		}

	}
    
    public void speakOut(){
    	mTTS.speak(speakableText, TextToSpeech.QUEUE_FLUSH, null);
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
        	String notificationTextOutput = "";
        	String tickerText = "";
			String tickerTextOutput = "";
        	String titleText = "";
        	Notification noti = null;
        	
        	if (intent.getStringExtra("notification_event") != null){
        		Log.d(TAG,"*******Received notification_event " + eventText);
        		eventText = intent.getStringExtra("notification_event");
        		String temp = eventText + txtView.getText();
        		txtView.setText(temp);;
        	} else {
            	if (intent.getStringExtra("notification_title") != null){
            		titleText = intent.getStringExtra("notification_title");
            	}
    			if (intent.getParcelableExtra("statusbar_notification_object") != null){
    				Log.d(TAG,"***parcelable received");
    				Parcelable parcel = intent.getParcelableExtra("statusbar_notification_object");
    				noti = (Notification) parcel;
    				if (noti.tickerText != null){
    					tickerTextOutput = "Ticker Text: " + noti.tickerText.toString() + "\n";
    					tickerText = noti.tickerText.toString();
						}
    				if (parcel instanceof Notification) {
    					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
    						notificationText += getExtraBigData(noti, notificationText.trim());
    						notificationTextOutput = "Notification Text: " + notificationText;
    					} else {
    						notificationText += getExtraData(noti, notificationText.trim());
    						notificationTextOutput = "Notification Text: " + notificationText;
    					}
    				}
    			}
    			if (titleText.equalsIgnoreCase("hangouts") && noti != null) {
    				parseHangoutsNotification(noti);
    			}
				
    			String temp = tickerTextOutput
    					+ notificationTextOutput + "\n"
						+ speakableText;
    			
    			if (temp == null || temp.equals("") || temp.equals("null")){
    				Log.d(TAG,"notificationText empty");
    			} else {
    				txtView.setText(temp);
    			}
        	}
        	
			
		
        }
		
		public void parseHangoutsNotification (Notification noti){
			String tickerText = noti.tickerText.toString();
			String notificationText = "";
			String senderOne = "";
			String senderTwo = "";
			String message = "";
			Pattern firstHangoutsSender = Pattern.compile("(?<=:\\s).*?(?=,)");
			Log.i(TAG,"Hangouts parse initiated. Last message is " + lastMessage);
			
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				notificationText += "Notification Text: "
						+ getExtraBigData(noti, notificationText.trim());
			} else {
				notificationText += "Notification Text: "
						+ getExtraData(noti, notificationText.trim());
			}
			
			//determining if this is a single conversation or consolidated message
			String s = tickerText.substring(0,1);
			if (isInteger(s)){
				// This is a consolidated message.
				//identifying the sender of this notification
				Matcher m = firstHangoutsSender.matcher(tickerText);
				while (m.find()){
					senderOne = m.group();
					Log.i(TAG,"senderOne set to " + senderOne);
				}
				 //Determine if it is from a single sender or part of a group conversation.
				if (senderOne.matches(".*\\s.*")){
					//establishing second stated sender so we can use as a delimiter for notification content extraction
					Pattern p2 = Pattern.compile("(?<=" + senderOne + ",\\s).*?\\s.*?(?=(,|$))");
					Matcher m2 = p2.matcher(tickerText);
					while (m2.find()){
						senderTwo = m2.group();
						Log.i(TAG,"senderTwo set to " + senderTwo);
					}
					//Message content extraction changes slightly if senderTwo is a group
					Pattern p3;
					if (senderTwo.matches(".*,.*")) {
						p3 = Pattern.compile("(?<=" + senderOne + "\\s\\s)[\\w\\W]*?(?=" + senderTwo + "\\s)");
						Log.i(TAG,"Second sender is a group");
					} else {
						p3 = Pattern.compile("(?<=" + senderOne + "\\s\\s)[\\w\\W]*?(?=\\s" + senderTwo + "\\s\\s)");
						Log.i(TAG,"Second sender is solo");
					}
					//extracting the contents of just this most recent message
					Matcher m3 = p3.matcher(notificationText);
					while (m3.find()){
						message = m3.group();
						Log.i(TAG,"message set to " + message);
					}
				} else {
					//Possibly give a generic notification. No way to extract content from group convo in consolidated notification.
					speakableText = "New group hangouts message.";
					speakOut();
					return;
				}
				//clearing lastMessage so that new thread won't get misread
				lastMessage = "";
			} else {
				//this is a standalone message or part of an ongoing convo thread
				//identify sender/group
				Pattern p4 = Pattern.compile("^.*(?=:)");
				Matcher m4 = p4.matcher(tickerText);
				while (m4.find()){
					senderOne = m4.group();
					Log.i(TAG,"senderOne set to " + senderOne);
				}
				//separate out content from sender in tickerText
				String tickerContent = "";
				Pattern p5 = Pattern.compile("(?<=:\\s).*");
				Matcher m5 = p5.matcher(tickerText);
				while (m5.find()){
					tickerContent = m5.group();
				}
				
				String notificationContent = "";
				Pattern p6 = Pattern.compile("(?<=" + senderOne + "\n" + lastMessage + ")" + "[\\w\\W]*");
				Matcher m6 = p6.matcher(notificationText);
				while (m6.find()) {
					notificationContent = m6.group();
					Log.i(TAG,"Notification Content set to " + notificationContent);
				}
				lastMessage += notificationContent;
				//Determining if this message is standalone
				if (tickerText.matches("\\d{1,2} new messages$")) {
					//its likely a message thread, but we need to be sure
					if (notificationText.equals(senderOne + "\n" + tickerContent)){
						//it's actually a solo message
						message = tickerContent;
						Log.i(TAG,"message set to " + message);
					}
					//its part of a message thread
					//Determine if it's solo or group message
					if (senderOne.matches(".*,.*")) {
						//it's a group message and sender must be extracted
						Pattern p7 = Pattern.compile("^.*(?=\\s)");
						Matcher m7 = p7.matcher(notificationContent);
						while (m7.find()) {
							senderOne = m7.group() + " in group hangout";
							Log.i(TAG,"senderOne set to " + senderOne);
						}
						//extracting message content
						Pattern p8 = Pattern.compile("(?<=" + senderOne + "\\s).*");
						Matcher m8 = p8.matcher(notificationContent);
						while (m8.find()) {
							message = m8.group();
							Log.i(TAG,"message set to " + message);
						}
					} else {
						//it's a solo sender
						message = notificationContent;
						Log.i(TAG,"message set to " + message);
					}
				} else {
					//it's a solo standalone message
					message = notificationContent;
					Log.i(TAG,"message set to " + message);
				}
			}
			speakableText = "New hangouts message from " + senderOne + "\n"
					+ message;
			speakOut();
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
