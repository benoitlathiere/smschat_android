package org.lathiere.smileschat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

//source : http://www.tutos-android.com/broadcast-receiver-android
//source : http://blog.valtech.fr/2010/05/06/detecteur-devenements-sous-android-lapplication-bigbrother/


public class SMSReceiver extends BroadcastReceiver {

	private static final String codePattern = "^(:?[#]\\w{6}[#])";			//"^(#)(\\d{3})(#)(.)";

	public void onReceive(Context context, Intent intent) {
		Log.w("",intent.getAction());
		if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
			Bundle pdusBundle = intent.getExtras();
			if (pdusBundle != null) {
				Object[] pdus = (Object[]) pdusBundle.get("pdus");
				SmsMessage messages = SmsMessage.createFromPdu((byte[]) pdus[0]);
				Log.d("", messages.getMessageBody() + " par " + messages.getOriginatingAddress());
				//if (messages.getMessageBody().contains("Hi")) {
				if (messages.getMessageBody().matches(codePattern)) {
					abortBroadcast();
					Log.i("", "Bingo !!!");
				}
			}
		}
	}
}
