package org.lathiere.smileschat;

import java.sql.Timestamp;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite Class.<br/>
 * source :
 * http://www.tutomobile.fr/comment-utiliser-sqlite-sous-android-tutoriel
 * -android-n%C2%B019/19/10/2010/ source :
 * http://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
 */
public class SQLiteClass extends SQLiteOpenHelper {

	private static final String TABLE_MESSAGES = ThreadDB.TABLE_MESSAGES;
	private static final String TABLE_THREADS = ThreadDB.TABLE_THREADS;

	public SQLiteClass(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	public SQLiteClass(Context context) {
		super(context, ThreadDB.DB_NAME, null, ThreadDB.DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// we create tables
		String req = " CREATE TABLE " + TABLE_THREADS
				+ " (ID INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ " contributors TEXT);";
		db.execSQL(req);
		req = " CREATE TABLE " + TABLE_MESSAGES
				+ " (ID INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ " tID INTEGER NOT NULL REFERENCES threads(ID) ON UPDATE CASCADE ON DELETE CASCADE, "
				+ " date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
				+ " text TEXT NOT NULL, "
				+ " contributor VARCHAR(15) NOT NULL)  ;";
		db.execSQL(req);
		ContentValues values = new ContentValues();
		values.put("contributors", "+3345678908, +3365754467");
		int newid = (int) (long) db.insert(TABLE_THREADS, null, values);
		values.clear();
		java.util.Date date = new java.util.Date();
		values.put("tID", newid);
		values.put("date", String.valueOf(new Timestamp(date.getTime())));
		values.put("text", "bonjour ! msg 1");
		values.put("contributor", "+334000000");
		db.insert(TABLE_MESSAGES, null, values);
		// Log.w("","ID inserted:"+String.valueOf( db.insert(TABLE_THREADS,
		// null, values))); //debug
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// if version number changes, we do it
		db.execSQL("DROP TABLE " + TABLE_MESSAGES + ";");
		db.execSQL("DROP TABLE " + TABLE_THREADS + ";");
		onCreate(db);
	}

}
