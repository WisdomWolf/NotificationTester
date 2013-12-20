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
	private String spokenText = "";
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
    	mTTS.speak(spokenText, TextToSpeech.QUEUE_FLUSH, null);
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
			String senderOne = "";
        	String senderTwo = "";
        	String hangoutsMessage = "";
        	
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
    				Notification noti = (Notification) parcel;
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
    			//identifying the sender of this notification
				Matcher m = firstHangoutsSender.matcher(tickerText);
				while (m.find()){
					senderOne = m.group();
				}
				
				//establishing second stated sender so we can use as a delimiter for notification content extraction
				Pattern p2 = Pattern.compile("(?<=" + senderOne + ",\\s).*?(?=(,|$))");
				Matcher m2 = p2.matcher(tickerText);
				while (m2.find()){
					senderTwo = m2.group();
				}
				
				//extracting the contents of just this most recent message
				Pattern p3 = Pattern.compile("(?<=" + senderOne + "\\s\\s)[\\w\\W]*?(?=\\s" + senderTwo + "\\s\\s)");
				Matcher m3 = p3.matcher(notificationText);
				while (m3.find()){
					hangoutsMessage = m3.group();
				}
				spokenText = "New " + titleText + " message from " + senderOne + "\n"
						+ hangoutsMessage;
				
    			String temp = tickerTextOutput
    					+ notificationTextOutput + "\n"
						+ spokenText;
    			
    			speakOut();
    			if (temp == null || temp.equals("") || temp.equals("null")){
    				Log.d(TAG,"notificationText empty");
    			} else {
    				txtView.setText(temp);
    			}
        	}
        	
			
		
        }
		
		public void parseHangoutsNotification (Notification noti, String pkgLabel){
			String tickerText = noti.tickerText.toString();
			String notificationText = "";
			
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
				/* Determine if it is from a single sender or part of a group conversation.
				//search tickerText and identify if there is a space in the first sender's name.  
				//Probably easiest to use existing regex to determine senderOne and search for whitespace (\\s) in senderOne
				//if (senderOne has space)
				 
				 */
			} else {
				//parse as single sender
				/*
				 * Identify sender from Ticker Text as all chars leading up to :
				 * Content begins immediately after sender + CRLF
				 * Alternatively could skip tickerText and just start content from second line
				 */
			}
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
