package org.lathiere.smileschat;

import java.sql.Timestamp;
import java.util.ArrayList;

import org.lathiere.smileschat.Thread.Msg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class TalkActivity extends Activity implements OnClickListener, OnLongClickListener, TextWatcher {

	// UI
	private TextView TV_msg;
	private EditText ET_msg;
	private Button BT_send;
	private ScrollView SV_messages;
	private LinearLayout LL_messages;

	// global
	private ThreadDB db = null;
	private static int threadID = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_talk);
		Log.d(Consts.DEBUG, "onCreate()");

		// UI
		TV_msg = (TextView) findViewById(R.id.TV_msg);
		ET_msg = (EditText) findViewById(R.id.ET_msg);
		ET_msg.addTextChangedListener(this);
		BT_send = (Button) findViewById(R.id.BT_send);
		BT_send.setOnClickListener(this);
		SV_messages = (ScrollView) findViewById(R.id.SV_messages);
		LL_messages = (LinearLayout) findViewById(R.id.LL_messages);

		db = new ThreadDB(getBaseContext());
		db.open();

		if (savedInstanceState != null)
			loadThread(savedInstanceState.getInt("threadID"));

		if (getIntent() != null && getIntent().getExtras() != null) {
			loadThread(getIntent().getIntExtra("threadID", 0));
			loadAction(getIntent().getIntExtra("action", 0));
		}
	}

	/**
	 * Analyse and execute action.
	 * @param action Action to execute.
	 */
	private void loadAction(int action) {
		Log.d(Consts.DEBUG, "loadAction()");
		if (action > 0) {
			switch (action) {
				case Consts.ACTION_LIST:		//TODO still usefull ?
					loadThread(threadID);
					break;
				case Consts.ACTION_ADD_MESSAGE:
					addMsg(getIntent().getExtras());
					break;
			}
		}
	}

	/**
	 * Retrieve and display messages from a thread.
	 * @param ID Thread ID (>0).
	 */
	private void loadThread(int ID) {
		Log.d(Consts.DEBUG, "loadThread() thread ID=" + String.valueOf(ID));
		if (ID > 0 && ID != threadID) {
			threadID=ID;
			Thread t = Thread.getThread(threadID);
			if (t!=null) {
				TV_msg.setText("#" + String.valueOf(threadID) + " - " + t.getContributors() + " ("+t.getLastUpdate()+")");
				LL_messages.removeAllViews();
				ArrayList<Msg> list = ThreadDB.getAllMsg(threadID);
				if (list != null) {
					for (int z = 0; z < list.size(); z++) {
						Msg msg = list.get(z);
						addMsg(msg);
						SV_messages.fullScroll(View.FOCUS_DOWN);
					}
				}
			} else {
				Log.e(Consts.DEBUG, "Thread doesn't exit.");
				Intent myIntent = new Intent(this, ThreadsActivity.class);
				myIntent.putExtra("action", Consts.ACTION_NO_THREAD);
				try {
					startActivity(myIntent);
					finish();
				} catch (android.content.ActivityNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Add a message to the talk.
	 * @param msg Complete message.
	 */
	private void addMsg(Msg msg) {
		Log.d(Consts.DEBUG, "addMsg(Msg)");
		String sender = msg.getContributor();
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(sender));
		Cursor cursor = getBaseContext().getContentResolver().query(uri, new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);
		if (cursor.moveToFirst())
			sender = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
		cursor.close();
		String tmp = "[" + msg.getID() + "]" + sender + " (" + msg.getReadableDate() + "): " + msg.getText();
		View header = null;
		TextView tvtmp = null;
		if (msg.getContributor().equalsIgnoreCase(Consts.getPhoneNumber(getBaseContext()))) {
			header = (View) getLayoutInflater().inflate(R.layout.message_me, null);
			tvtmp = (TextView) header.findViewById(R.id.Tv_message_me);
			registerForContextMenu(header);
		} else {
			header = (View) getLayoutInflater().inflate(R.layout.message_other, null);
			tvtmp = (TextView) header.findViewById(R.id.Tv_message_other);
		}
		tvtmp.setText(tmp);
		header.setId(msg.getID());
		LL_messages.addView(header);
		SV_messages.fullScroll(View.FOCUS_DOWN);
	}

	/**
	 * Add a message to the talk. If thread doesn't exist, activity is closed.
	 * @param extras
	 */
	private void addMsg(Bundle extras) {
		Log.d(Consts.DEBUG, "addMsg(Bundle) extras: " + extras.toString());
		String text = extras.getString("text");
		String sender = extras.getString("sender");
		Timestamp timestamp = new Timestamp(extras.getLong("timestamp", 0));
		int tID = extras.getInt("threadID", 0);
		if (Thread.threadExists(tID)) {
			Msg msg;
			if (text != null && text.trim().length()>0 && sender != null && timestamp != null && tID != 0) {
				msg = Thread.Msg.addMessage(tID, text, sender, timestamp);
				if (msg != null)
					addMsg(msg);
			} else {
				Log.e("", "addMsg() empty elements : " + text + " / " + sender + " / " + String.valueOf(tID));
			}
		} else {
			Log.e(Consts.DEBUG, "[2] Thread doesn't exit. extras:"+extras.toString());
			Intent myIntent = new Intent(this, ThreadsActivity.class);
			myIntent.putExtra("action", Consts.ACTION_NO_THREAD);
			try {
				startActivity(myIntent);
				finish();
			} catch (android.content.ActivityNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	private void newMsg() {
		if (! Consts.isGSMAvailable(getBaseContext())) {
			Toast.makeText(getApplicationContext(), getString(R.string.msg_operator_unavailable), Toast.LENGTH_SHORT).show();
		} else {
			String name = ET_msg.getText().toString();
			if (name != "") {
				ET_msg.setText("");
				SendSMS.sendSMS(this, threadID, name, null); // FIXME change dest
			} else {
				ET_msg.setError(getString(R.string.msg_not_empty));
			}
		}
	}

	/**
	 * Activity menu.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.talk, menu);
		return true;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.d(Consts.DEBUG, "onNewIntent()");
		if ((intent.getExtras() != null) && (intent.getIntExtra("action", 0) == Consts.ACTION_ADD_MESSAGE)) {
			int ID = intent.getIntExtra("threadID", 0);
			Thread t=Thread.getThread(ID);
			if (t != null) {
				loadThread(ID);
				addMsg(intent.getExtras());
			} else {
				Log.e(Consts.DEBUG, "[1] Thread doesn't exit. extra:"+intent.getExtras().toString());
				Intent myIntent = new Intent(this, ThreadsActivity.class);
				myIntent.putExtra("action", Consts.ACTION_NO_THREAD);
				try {
					startActivity(myIntent);
					finish();
				} catch (android.content.ActivityNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.d(Consts.DEBUG, "onConfigurationChanged()");
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(Consts.DEBUG, "onPause()");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(Consts.DEBUG, "onResume() ");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d(Consts.DEBUG, "onSaveInstanceState() ");
		outState.putInt("threadID", threadID);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Log.d(Consts.DEBUG, "onRestoreInstanceState() ");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(Consts.DEBUG, "onDestroy()");
		setIntent(null);	//FIXME ??
		db.close();
	}

	@Override
	public void onClick(View v) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS); // hide keyboard
		Log.d(Consts.DEBUG, "onClick()");
		switch (v.getId()) {
			case (R.id.BT_send):
				newMsg();
				break;
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		Log.d(Consts.DEBUG, "onCreateContextMenu()");
		if (((View) v.getParent()).getId() == LL_messages.getId()) {
			menu.setHeaderTitle("azertyu");
			menu.add(v.getId(), Consts.MENU_DELETE, Consts.MENU_DELETE, R.string.label_delete);
		}
	}

	/**
	 * API : event on context menu item.
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Log.d(Consts.DEBUG, "onContextItemSelected()");
		switch (item.getItemId()) {
			case Consts.MENU_DELETE:
				if (Thread.Msg.deleteMessage(item.getGroupId())) // TODO ok?
					LL_messages.removeView(findViewById(item.getGroupId())); // FIXME ok?
				break;
			default:
				Log.e("", "unknown menu element: " + item.getTitle());
		}
		return true;
	}

	@Override
	public boolean onLongClick(View v) {
		Log.d(Consts.DEBUG, "onLongClick()");
		Log.w(Consts.DEBUG, String.valueOf(LL_messages.getId()) + " / " + String.valueOf(((View) v.getParent()).getId()));
		if (((View) v.getParent()).getId() == LL_messages.getId()) {
		}
		return false;
	}

	@Override
	public void afterTextChanged(Editable arg0) {
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		//Log.d(Consts.DEBUG, "changed!");
		if (ET_msg.getText().toString().trim().length()>0)
			BT_send.setEnabled(true);
		else
			BT_send.setEnabled(false);
		/*Log.i(Consts.DEBUG,"charsequence:"+ String.valueOf(s.charAt(s.length()-1)));
		if (String.valueOf(s.charAt(s.length()-1))=="\n") {
			newMsg();
		}*/
	}

}