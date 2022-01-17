package org.lathiere.smileschat;

import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;

public final class Consts {

	// dev
	public final static String DEBUG = "smileschat-debug";

	// menus
	public static final int MENU_LIST_MSG = 3;
	public static final int MENU_DELETE = 5;

	// action from Intent/Activity
	public static final int ACTION_LIST = 1; // FIXME usefull ??
	public static final int ACTION_ADD_MESSAGE = 2;
	public static final int ACTION_NO_THREAD = 3;

	//SharedPreferences général :
	public static final String PREFS_FILENAME = "AppPrefs";		//prfs file
	public static final int PREFS_MODE = 0;						//0 = MODE_PRIVATE

	// misc
	public static final Pattern codePattern = Pattern.compile("^&#(\\d*)#"); //ex: &#123456#bonjour tout le monde.

	/**
	 * Return the user phone number.
	 * @param context Context.
	 * @return Phone number or <code>null</code>.
	 */
	public static final String getPhoneNumber(Context context) {
		String myPhoneNumber=null;
		//prefs ?
		SharedPreferences prefs;
		prefs = context.getSharedPreferences(PREFS_FILENAME, PREFS_MODE);
		try {
			myPhoneNumber = prefs.getString("myPhoneNumber", null);
			if (myPhoneNumber != null)
				return myPhoneNumber;
		} catch(ClassCastException e) {}
		//in SIM card ?
		TelephonyManager tMgr =(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		myPhoneNumber = tMgr.getLine1Number();
		if (myPhoneNumber != null)
			return myPhoneNumber;

		/*if (android.os.Build.MANUFACTURER.toLowerCase(Locale.getDefault()).contains("samsung"))
			return "+33664916989, +33652275877";	//FIXME
		return "+33664916989; +33652275877";	//FIXME*/
		//return "+33664916989";
		return null;
	}

	/**
	 * Save the user's phone number.
	 * @param context Context.
	 * @param number
	 * @return Saving state.
	 */
	public static final boolean setPhoneNumber(Context context, String number) {
		SharedPreferences prefs;
		prefs = context.getSharedPreferences(PREFS_FILENAME, PREFS_MODE);
		SharedPreferences.Editor editor=prefs.edit();
		Log.d(Consts.DEBUG, "number to save :"+number);
		return editor.putString("myPhoneNumber", number).commit();
	}

	/**
	 * Indicates if the GSM network is accessible.
	 * @param context Context.
	 * @return Accessibility of the GSM network.
	 */
	@SuppressWarnings("deprecation")
	public static final boolean isGSMAvailable(Context context) {
		/*TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		Log.d(Consts.DEBUG, "phone type: "+String.valueOf(tm.getPhoneType())+" / SIM state: "+String.valueOf(tm.getSimState()));
		if (tm.getPhoneType()==TelephonyManager.PHONE_TYPE_GSM && tm.getSimState()==TelephonyManager.SIM_STATE_READY) {
			Log.d(Consts.DEBUG, "réseau !");
			return true;
		}*/
		//check provider
		ServiceState serviceState = new ServiceState();
		if (serviceState.getState()==ServiceState.STATE_IN_SERVICE)
			return true;
		//check mode plane
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			if (Settings.System.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0)==0)
				return true;
		} else {
			if (Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0)==0)
				return true;
		}
		return false;
	}

}
