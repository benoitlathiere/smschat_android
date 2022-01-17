package org.lathiere.smileschat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class ThreadsActivity extends Activity implements OnClickListener, OnItemClickListener, TextWatcher, android.content.DialogInterface.OnClickListener {

	// UI
	private ListView LVThreads;
	private ArrayAdapter<Thread> thAdapt;
	private ThreadDB db=null;
	private Button BTNewThread;
	private EditText dialoginput;

	private MultiAutoCompleteTextView ACTVNameThread;
	private ArrayList<Map<String, String>> mPeopleList;
	private SimpleAdapter contactsAdapter;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_threads);
		Log.i(Consts.DEBUG, "ThreadsActivity.onCreate()");

		// UI
		LVThreads = (ListView) findViewById(R.id.LVThreads);
		BTNewThread = (Button) findViewById(R.id.BTNewThread);
		BTNewThread.setOnClickListener(this);
		ACTVNameThread = (MultiAutoCompleteTextView) findViewById(R.id.ACTVNameThread);
		ACTVNameThread.addTextChangedListener(this);
		//ACTVNameThread.setOnItemClickListener(this);

		if (db==null) {
			db = new ThreadDB(getBaseContext());
			db.open();
		}
		listThreads();

		Toast.makeText(getApplicationContext(), getString(R.string.label_phone_number)+": "+Consts.getPhoneNumber(getBaseContext()), Toast.LENGTH_SHORT).show();	//FIXME

		if (getIntent() != null && getIntent().getExtras() != null) {
			extras(getIntent().getExtras());
		}

		//contacts
		mPeopleList = new ArrayList<Map<String, String>>();
	    PopulatePeopleList();
	    //contactsAdapter = new SimpleAdapter(this, mPeopleList, R.layout.contactslist, new String[] { "Name", "Phone", "Type" }, new int[] { R.id.ccontName, R.id.ccontNo, R.id.ccontType });	//FIXME ok?
	    contactsAdapter = new SimpleAdapter(this, mPeopleList, R.layout.contactslist, new String[] {"Name", "Phone"}, new int[] {R.id.ccontName, R.id.ccontNo});
	    //contactsAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mPeopleList);	//bad
	    ACTVNameThread.setAdapter(contactsAdapter);
	    ACTVNameThread.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
	    Log.i(Consts.DEBUG,"mPeopleList size="+String.valueOf(mPeopleList.size()));
	    ACTVNameThread.setOnItemClickListener(new OnItemClickListener() {
	        @Override
	        public void onItemClick(AdapterView<?> av, View arg1, int index, long arg3) {
	            Map<String, String> map = (Map<String, String>) av.getItemAtPosition(index);
	            String name  = map.get("Name");
	            String number = map.get("Phone");
	            Log.i(Consts.DEBUG, "ACTVNameThread text: "+ACTVNameThread.getText().toString());	//.replaceAll("\\{*\\}", ""));	//TODO?
	            //ACTVNameThread.setText(name+" <"+number+">; "+ ACTVNameThread.getText().toString());//ACTVNameThread.getText()
	        }
	    });

	    //test2
	    //String[] countries = getResources().getStringArray(R.array.countries_array);
	    //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mPeopleList);

		//source : http://stackoverflow.com/questions/11934283/autocomplete-textview-with-contacts
		/*ArrayList<String> emailAddressCollection = new ArrayList<String>();
		ContentResolver cr = getContentResolver();
		Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
		while (emailCur.moveToNext())
		{
		    String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
		    emailAddressCollection.add(email);
		}
		emailCur.close();
		String[] emailAddresses = new String[emailAddressCollection.size()];
		emailAddressCollection.toArray(emailAddresses);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, emailAddresses);
		//MultiAutoCompleteTextView textView = (MultiAutoCompleteTextView)findViewById(R.id.ACTVNameThread);
		ACTVNameThread.setAdapter(adapter);*/

	}

	public void PopulatePeopleList() {
	    mPeopleList.clear();
	    Cursor people = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
	    while (people.moveToNext()) {
	        String contactName = people.getString(people.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
	        String contactId = people.getString(people.getColumnIndex(ContactsContract.Contacts._ID));
	        String hasPhone = people.getString(people.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
	        if ((Integer.parseInt(hasPhone) > 0)){
	            // You now have the number so now query it like this
	            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId, null, null);
	            while (phones.moveToNext()){
	                //store numbers and display a dialog letting the user select which.
	                String phoneNumber = phones.getString(
	                phones.getColumnIndex(
	                ContactsContract.CommonDataKinds.Phone.NUMBER));
	                String numberType = phones.getString(phones.getColumnIndex(
	                ContactsContract.CommonDataKinds.Phone.TYPE));
	                Map<String, String> NamePhoneType = new HashMap<String, String>();
	                NamePhoneType.put("Name", contactName);
	                NamePhoneType.put("Phone", phoneNumber);
	                if(numberType.equals("0"))
	                    NamePhoneType.put("Type", "Work");
	                else if(numberType.equals("1"))
	                    NamePhoneType.put("Type", "Home");
	                else if(numberType.equals("2"))
	                    NamePhoneType.put("Type",  "Mobile");
	                else
	                    NamePhoneType.put("Type", "Other");
	                //Then add this map to the list.
	                mPeopleList.add(NamePhoneType);
	            }
	            phones.close();
	        }
	    }
	    people.close();
	    //startManagingCursor(people);	//FIXME very bad
	}




	/**
	 * List all threads in the ListView.
	 */
	private void listThreads() {
		Log.d(Consts.DEBUG, "listThreads()");
		LVThreads.setAdapter(null);
		ArrayList<Thread> list = Thread.listAllThreads();
		if (list != null) {
			thAdapt = new ArrayAdapter<Thread>(this, android.R.layout.simple_list_item_1, list);
			if (thAdapt != null) {
				LVThreads.setAdapter(thAdapt);
				registerForContextMenu(LVThreads);
				LVThreads.setOnItemClickListener(this);
				LVThreads.setSelection(LVThreads.getCount() - 1);
			}
		}
	}

	private void extras(Bundle extras) {
		Log.i(Consts.DEBUG, "ThreadsActivity.extras()");
		if (extras != null) {
			switch (extras.getInt("action", 0)) {
				case (Consts.ACTION_NO_THREAD):
					Toast.makeText(getApplicationContext(), R.string.msg_no_thread , Toast.LENGTH_LONG).show();
					break;
				default:
					Log.d(Consts.DEBUG, "ThreadsActivity.onNewIntent() no value!?");
			}
		}
	}

	/**
	 * Add an new thread to the list.
	 */
	private void addThread() {
		String contributors = ACTVNameThread.getText().toString();
		ACTVNameThread.setText("");
		Log.d("", "contributors:" + contributors);
		if (contributors.length() > 0) {
			Thread t = Thread.createThread(contributors);
			if (t != null) {
				//listThreads();
				Intent myIntent = new Intent(this, TalkActivity.class);
				myIntent.putExtra("threadID", t.getID());
				try {
					startActivity(myIntent);
				} catch (android.content.ActivityNotFoundException e) {
					e.printStackTrace();
					listThreads();
				}
			}
		}
	}


	private void popupPhoneNumber() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(getString(R.string.label_phone_number));
		alert.setMessage("Your phone number is :\n"+Consts.getPhoneNumber(this)+"\n\nYou can change it :");	//FIXME text
		dialoginput = new EditText(this);	// Set an EditText view to get user input
		dialoginput.setText(Consts.getPhoneNumber(this));
		alert.setView(dialoginput);
		alert.setNeutralButton("Close", this);
		alert.setPositiveButton("Save", this);
		alert.show();
	}


    /*private Cursor getContacts()
    {
        // Run query
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String[] projection = new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME };
        String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '" + (mShowInvisible ? "0" : "1") + "'";
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
        return managedQuery(uri, projection, selection, selectionArgs, sortOrder);
    }*/


    /*private void populateContactList() {
        // Build adapter with contact entries
        Cursor cursor = getContacts();
        String[] fields = new String[] { ContactsContract.Data.DISPLAY_NAME };
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.contact_entry, cursor, fields, new int[] {R.id.contactEntryText});
        mContactList.setAdapter(adapter);
    }*/



	/**
	 * Activity menu.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.threads, menu);
		return true;
	}



	/**
	 * onClick event.
	 */
	@Override
	public void onClick(View v) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS); // hide keyboard
		switch (v.getId()) {
		case (R.id.BTNewThread):
			addThread();
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View v, int pos, long id) {
		Log.i(Consts.DEBUG, "ThreadsActivity.onItemClick() "+String.valueOf(v.getId())+" / "+String.valueOf(R.id.LVThreads));
		//if (v.getId() == R.id.LVThreads) {
				Intent myIntent = new Intent(this, TalkActivity.class);
				myIntent.putExtra("threadID", ((Thread) adapter.getItemAtPosition(pos)).getID());
				startActivity(myIntent);
		//}
		/*else if (adapter == contactsAdapter) {
				Map<String, String> map = (Map<String, String>) adapter.getItemAtPosition(pos);
	            String name  = map.get("Name");
	            String number = map.get("Phone");
	            Log.i(Consts.DEBUG,"Contact:"+map.toString());
	            ACTVNameThread.setText(""+name+"<"+number+">");
		}*/

	}

	/**
	 * API : Context MEnu on Listview item (see registerForContextMenu())
	 * @param menu Menu
	 * @param v View
	 * @param menuInfo
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		if (v.getId() == R.id.LVThreads) {
			Thread th = (Thread) LVThreads.getItemAtPosition(info.position);
			menu.setHeaderTitle(th.getContributors());
			ArrayList<Thread.Msg> tmp = th.listAllMsg();
			if (tmp != null) {
				SubMenu submsg = menu.addSubMenu(th.getID(), Consts.MENU_LIST_MSG, Consts.MENU_LIST_MSG, R.string.label_list_messages);
				submsg.setHeaderTitle("Messages");
				for (int z = 0; z < tmp.size(); z++)
					submsg.add(Menu.NONE, tmp.get(z).getID(), z, tmp.get(z).toString());
			}
			menu.add(th.getID(), Consts.MENU_DELETE, Consts.MENU_DELETE, R.string.label_delete);
		} else {
			Log.w(Consts.DEBUG, "onCreateContextMenu() empty");
		}
	}

	/**
	 * API : event on context menu item
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Log.i(Consts.DEBUG, "ThreadsActivity.onContextItemSelected()");
		switch (item.getItemId()) {
		case Consts.MENU_DELETE:
			if (Thread.deleteThread(item.getGroupId()))
				listThreads();
			break;
		default:
			Log.w(Consts.DEBUG, "Elément inconnu du menu contextuel: " + item.getTitle());
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(Consts.DEBUG, "ThreadsActivity.onOptionsItemSelected()");
		switch (item.getItemId()) {
		}
		return false;
	}


	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Log.i(Consts.DEBUG, "ThreadsActivity.onMenuItemSelected()");
		switch (item.getItemId()) {
			case Consts.MENU_DELETE:
				if (Thread.deleteThread(item.getGroupId()))
					listThreads();
				break;
			case (R.id.ItemMyPhoneNumber):
				popupPhoneNumber();
				break;
		}
		return false;
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i(Consts.DEBUG, "ThreadsActivity.onPause()");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i(Consts.DEBUG, "ThreadsActivity.onResume() ");
		/*if (db==null) {
			db = new ThreadDB(getBaseContext());
			db.open();
		}
		listThreads();*/
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(Consts.DEBUG, "ondestroy() ");
		db.close();
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (ACTVNameThread.getText().toString().trim().length()>0)
			BTNewThread.setEnabled(true);
		else
			BTNewThread.setEnabled(false);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.i(Consts.DEBUG, "ThreadsActivity.onNewIntent()");
		extras(intent.getExtras());
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		dialog.dismiss();
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE:	//save
				if (Consts.setPhoneNumber(this, dialoginput.getText().toString().trim()))
					Toast.makeText(this, R.string.label_changes_ok, Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(this, R.string.label_changes_not_ok, Toast.LENGTH_LONG).show();
				break;
		}
	}


}
