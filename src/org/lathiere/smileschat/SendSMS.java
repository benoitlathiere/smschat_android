package org.lathiere.smileschat;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

public class SendSMS {

	public static void sendSMS(Context context, int threadID, String text, String dest) {
		String globalText = "&#" + threadID +"#"+ text;	//&#123456#
		if (dest == null)
			dest = Consts.getPhoneNumber(context); // FIXME collect dest numbers.
		ArrayList<String> distList = new ArrayList<String>();
		distList.add("+33664916989");
		distList.add("+33652275877");
		try {
			SmsManager smsManager = SmsManager.getDefault();
			PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, new Intent("SMS_SENT"), 0); // TODO
			for (String i : distList) {
				smsManager.sendTextMessage(i, null, globalText, null, sentPI);
			}
			//smsManager.sendTextMessage(dest, null, globalText, null, sentPI);
			Log.d(Consts.DEBUG, "SMS sent:" + globalText + " To:" + distList.toString());
			// smsManager.
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

}
