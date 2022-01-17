package org.lathiere.smileschat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

//source : http://www.tutos-android.com/broadcast-receiver-android
//source : http://blog.valtech.fr/2010/05/06/detecteur-devenements-sous-android-lapplication-bigbrother/

public class SMSReceiver extends BroadcastReceiver {

	//private static final String codePattern = "^(:?[#]\\w{6}[#])"; // "^(#)(\\d{3})(#)(.)";
	//source : http://www.regexr.com/
	private static final Pattern codePattern = Pattern.compile("^&#(\\d*)#"); //ex: &#123456#bonjour tout le monde.	TODO


	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(Consts.DEBUG, "onReceive()");
		if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
			Bundle pdusBundle = intent.getExtras();
			if (pdusBundle != null) {
				Object[] pdus = (Object[]) pdusBundle.get("pdus");
				SmsMessage messages = SmsMessage.createFromPdu((byte[]) pdus[0]);
				Log.d(Consts.DEBUG, messages.getMessageBody() + " by " + messages.getOriginatingAddress());
				try {
					Matcher matcher = codePattern.matcher(messages.getMessageBody());
					//if (messages.getMessageBody().startsWith("&&")) {	//old
					if (matcher.find()) {
						abortBroadcast();
						//Log.i(Consts.DEBUG, "tID+Message : " + messages.getMessageBody().substring(2));	//old school
						Log.i(Consts.DEBUG, "tID : " + matcher.group(1));
						Intent i = new Intent();
						i.setClassName(context.getPackageName(), context.getPackageName() + ".TalkActivity");
						// i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); //good
						i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
						//i.putExtra("threadID", Integer.parseInt(messages.getMessageBody().substring(2, 3)));	//old school
						try {
							i.putExtra("threadID", Integer.parseInt(matcher.group(1)));
							i.putExtra("text", messages.getMessageBody().substring(matcher.group(0).length()));
							i.putExtra("sender", messages.getOriginatingAddress());
							i.putExtra("timestamp", messages.getTimestampMillis());
							i.putExtra("action", Consts.ACTION_ADD_MESSAGE);
							context.startActivity(i);
						} catch (NumberFormatException e) {
							Log.e(Consts.DEBUG, "error during SMS parsing !!");
							e.printStackTrace();
						}
					}
				} catch (NullPointerException e) {
					e.printStackTrace();
				} catch (ActivityNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
