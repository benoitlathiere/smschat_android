package org.lathiere.smileschat;

import java.sql.Timestamp;
import java.util.ArrayList;

import org.lathiere.smileschat.Thread.Msg;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Manipulate database to store/retrieve datas. source :
 * http://www.tutomobile.fr
 * /comment-utiliser-sqlite-sous-android-tutoriel-android-n%C2%B019/19/10/2010/
 */
public class ThreadDB {

	// DB
	public static final int DB_VERSION = 6;
	public static final String DB_NAME = "smileschat.db";
	public static final String TABLE_THREADS = "threads";
	public static final String TABLE_MESSAGES = "messages";

	// connectors
	private static SQLiteDatabase db=null;
	private SQLiteClass myBase;

	public ThreadDB(Context context) {
		myBase = new SQLiteClass(context, DB_NAME, null, DB_VERSION);
	}

	protected void open() {
		db = myBase.getWritableDatabase();
	}

	public void close() {
		// on ferme l'accès à la BDD
		db.close();
	}

	public SQLiteDatabase getDB() {
		return db;
	}

	/* all CRUD operations */

	/**
	 * Get a thread from the database.
	 * @param ID Thread ID
	 * @return Thread or <code>null</code> if no thread or error.
	 */
	public static Thread getThread(int ID) {
		if (ID > 0) {
			//Cursor cursor = db.query(TABLE_THREADS, new String[] { "ID", "contributors" }, " ID LIKE \"" + ID + "\"", null, null, null, null);
			Cursor cursor = db.query(TABLE_THREADS, new String[] { "ID", "contributors" }, " ID='"+ID+"'", null, null, null, null);
			if (cursor.moveToFirst()) {
				Thread t = new Thread(cursor.getInt(0), cursor.getString(1));
				cursor.close();
				return t;
			}
		}
		return null;
	}

	/**
	 * List all threads from database.
	 * @return List in descending order, or <code>null</code> if error.
	 */
	public static ArrayList<Thread> getAllThreads() {
		/*if (db.isOpen()!=true)
			db=myBase.getWritableDatabase();*/
		ArrayList<Thread> tmp = null;
		Cursor cursor = db.query(TABLE_THREADS, new String[] { "ID", "contributors" }, null, null, null, null, "ID DESC");
		if (cursor.moveToFirst()) {
			tmp = new ArrayList<Thread>();
			do {
				tmp.add(new Thread(cursor.getInt(0), cursor.getString(1)));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return tmp;
	}

	/**
	 * Get the date of the last message from the database.
	 * @param threadID Thread ID
	 * @return Date or <code>empty</code> if error.
	 */
	public static Timestamp getLastUpdate(int threadID) {
		Cursor cursor = db.query(TABLE_MESSAGES, new String[] {"date"}, "tID=\""+threadID+"\"", null, null, null,"date DESC", "1");
		if (cursor.moveToFirst()) {
			try {
				Timestamp date = Timestamp.valueOf(cursor.getString(0));
				cursor.close();
				return date;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		cursor.close();
		return null;
	}

	/**
	 * Create a new thread in the database.
	 *
	 * @param contributors
	 * @return New ID or <code>0</code> if error.
	 */
	public static int createThread(String contributors) {
		ContentValues values = new ContentValues();
		values.put("contributors", contributors);
		Long res = db.insert(TABLE_THREADS, null, values);
		if (res != -1)
			return (int) (long) res;
		return 0;
	}

	/**
	 * Delete thread and its messages in the database.
	 * @param threadID Thread ID.
	 * @return State. <code>true</code> if any row deleted, else <code>false</code>.
	 */
	public static boolean deleteThread(int threadID) {
		return (db.delete(TABLE_THREADS, "ID=" + threadID, null) > 0);
	}

	/**
	 * Checks if a thread exists in the DB.
	 * @param threadID Thread ID.
	 * @return <code>true</code> if any row exists, else <code>false</code>.
	 */
	public static boolean threadExists(int threadID) {
		return db.query(TABLE_THREADS, new String[] {"ID"}, " ID='"+threadID+"' ", null, null, null, null).getCount()>0;
	}

	/**
	 * Insert new message in the database.
	 * @param threadID Thread ID.
	 * @param text Text.
	 * @param contributor Contributor.
	 * @param time Timestamp (<code>null</code> to use current timestamp).
	 * @return <code>Msg</code> Object or <code>null</code> if error.
	 */
	public static Msg addMessage(int threadID, String text, String contributor, Timestamp time) {
		ContentValues values = new ContentValues();
		values.put("tID", threadID);
		if (time == null)
			time = new Timestamp(new java.util.Date().getTime());
		values.put("date", String.valueOf(time));
		values.put("text", text);
		values.put("contributor", contributor);
		int newid = (int) (long) db.insert(TABLE_MESSAGES, null, values);
		if (newid == -1)
			return null;
		return new Msg(newid, threadID, time, text, contributor);
	}

	/**
	 * List all messages for a thread from database.
	 * @param threadID
	 * @return List of <code>Msg</code> or <code>null</code> if error.
	 */
	protected static ArrayList<Msg> getAllMsg(int threadID) {
		ArrayList<Msg> tmp = null;
		Cursor cursor = db.query(TABLE_MESSAGES, new String[] { "ID", "tID", "date", "text", "contributor" }, " tID LIKE \"" + threadID + "\"", null, null, null, "date ASC", null);
		if (cursor.moveToFirst()) {
			tmp = new ArrayList<Msg>();
			do {
				tmp.add(new Msg(cursor.getInt(0), cursor.getInt(1), Timestamp.valueOf(cursor.getString(2)), cursor.getString(3), cursor.getString(4)));
			} while (cursor.moveToNext());
			cursor.close();
		}
		return tmp;
	}

	public static boolean deleteMessage(int messageID) {
		Log.w("", "We delete message #" + messageID);
		if (db.delete(TABLE_MESSAGES, "ID=" + messageID, null) > 0)
			return true;
		return false;
	}

	/*
	 * public long insertLivre(Thread thread){ //Création d'un ContentValues
	 * (fonctionne comme une HashMap) ContentValues values = new
	 * ContentValues(); //on lui ajoute une valeur associé à une clé (qui est le
	 * nom de la colonne dans laquelle on veut mettre la valeur)
	 * //values.put(COL_ISBN, thread.getIsbn()); //values.put(COL_TITRE,
	 * thread.getTitre()); //on insère l'objet dans la BDD via le ContentValues
	 * return db.insert(TABLE, null, values); } public int updateLivre(int id,
	 * Thread thread){ //La mise à jour d'un livre dans la BDD fonctionne plus
	 * ou moins comme une insertion //il faut simple préciser quelle livre on
	 * doit mettre à jour grâce à l'ID ContentValues values = new
	 * ContentValues(); //values.put(COL_ISBN, thread.getIsbn());
	 * //values.put(COL_TITRE, thread.getTitre()); //return db.update(TABLE,
	 * values, COL_ID + " = " +id, null); return 0; } public int
	 * removeLivreWithID(int id){ //Suppression d'un livre de la BDD grâce à
	 * l'ID //return db.delete(TABLE, COL_ID + " = " +id, null); return 0; }
	 * public Thread getLivreWithTitre(String titre){ //Récupère dans un Cursor
	 * les valeur correspondant à un livre contenu dans la BDD (ici on
	 * sélectionne le livre grâce à son titre) //Cursor c = db.query(TABLE, new
	 * String[] {COL_ID, COL_ISBN, COL_TITRE}, COL_TITRE + " LIKE \"" + titre
	 * +"\"", null, null, null, null); //return cursorToLivre(c); return null; }
	 * //Cette méthode permet de convertir un cursor en un livre private Thread
	 * cursorToLivre(Cursor c){ //si aucun élément n'a été retourné dans la
	 * requête, on renvoie null if (c.getCount() == 0) return null; //Sinon on
	 * se place sur le premier élément c.moveToFirst(); //On créé un livre
	 * //Thread thread = new Thread(); //on lui affecte toutes les infos grâce
	 * aux infos contenues dans le Cursor //thread.setId(c.getInt(NUM_COL_ID));
	 * //thread.setIsbn(c.getString(NUM_COL_ISBN));
	 * //thread.setTitre(c.getString(NUM_COL_TITRE)); //On ferme le cursor
	 * c.close(); //On retourne le livre //return thread; return null; }
	 */

}
