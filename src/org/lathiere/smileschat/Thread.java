package org.lathiere.smileschat;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import android.util.Log;

public class Thread {

	private int ID;
	private String contributors;

	public Thread(int ID, String contributors) {
		this.ID = ID;
		this.contributors = contributors;
	}

	static public class Msg {
		private int ID;
		private int tID;
		private Timestamp date;
		private String text;
		private String contributor;

		/**
		 * Constructor
		 *
		 * @param ID Message ID (from database).
		 * @param tID Thread ID.
		 * @param date Timestamp.
		 * @param text Text.
		 * @param contructor Contributor.
		 */
		public Msg(int ID, int tID, Timestamp date, String text,
				String contructor) {
			this.ID = ID;
			this.tID = tID;
			this.date = date;
			this.text = text;
			this.contributor = contructor;
		}

		protected int getID() {
			return this.ID;
		}

		protected int getThreadID() {
			return this.tID;
		}

		protected String getContributor() {
			return this.contributor;
		}

		protected String getText() {
			return this.text;
		}

		/**
		 * Get Human-readable date (dd/MM/yyyy).
		 *
		 * @return Extended version of date.
		 */
		protected String getReadableDate() {
			return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(this.date);
		}

		/**
		 * Insert new message in the database.
		 *
		 * @param threadID
		 *            Thread ID.
		 * @param text
		 *            Text.
		 * @param contributor
		 *            Contributor.
		 * @param time
		 *            Timestamp (<code>null</code> to use current timestamp).
		 * @return <code>Msg</code> Object or <code>null</code> if error.
		 */
		public static Msg addMessage(int threadID, String text,
				String contributor, Timestamp time) {
			return ThreadDB.addMessage(threadID, text, contributor, time);
		}

		/**
		 * Delete a message.
		 *
		 * @param MessageID
		 *            Message ID.
		 * @return <code>True</code> if OK.
		 */
		public static boolean deleteMessage(int MessageID) {
			return ThreadDB.deleteMessage(MessageID);
		}

		@Override
		public String toString() {
			return "¤ " + this.ID + ": " + this.text + "(" + this.contributor
					+ ":" + this.getReadableDate() + ")";
		}
	}

	protected int getID() {
		return this.ID;
	}

	protected int getNbMsg() {
		ArrayList<Msg> tmp = listAllMsg();
		if (tmp == null)
			return 0;
		return listAllMsg().size();
	}

	/**
	 * Returns the human-readable date of the last message.
	 * @return Date (dd/mm/yyyy) or <code>empty</code> if error or no message.
	 */
	protected String getLastUpdate() {
		Timestamp date = ThreadDB.getLastUpdate(this.ID);
		try {
			return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * Retrieve participants to a thread.
	 * @return Participants.
	 */
	protected String getContributors() {
		return this.contributors;
	}

	/**
	 * Retrieve participants to a thread.
	 * @return List of participants.
	 */
	protected String[] getContributorsList() {
		try {
			return this.contributors.trim().replace(" ", "").replace(";", ",").split(",");
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get a thread.
	 * @param ID Thread ID
	 * @return Thread or <code>null</code> if no thread or error.
	 */
	public static Thread getThread(int ID) {
		if (ID > 0)
			return ThreadDB.getThread(ID);
		return null;
	}

	/**
	 * List all threads.
	 * @return List of <code>Thread</code> or <code>null</code> if error.
	 */
	public static ArrayList<Thread> listAllThreads() {
		ArrayList<Thread> tmp = ThreadDB.getAllThreads();
		return tmp;
	}

	/**
	 * List all messages in a thread.
	 * @return List or <code>null</code> if error.
	 */
	public ArrayList<Msg> listAllMsg() {
		return ThreadDB.getAllMsg(this.ID);
	}

	/**
	 * Checks if a thread exists.
	 * @param threadID Thread ID.
	 * @return <code>true</code> if exists, else <code>false</code>.
	 */
	public static boolean threadExists(int threadID) {
		return ThreadDB.threadExists(threadID);
	}

	/**
	 * Create new thread.
	 * @param contributors Phone numbers.
	 * @return <code>Thread</code>, or <code>null</code> if error.
	 */
	public static Thread createThread(String contributors) {
		int id = ThreadDB.createThread(contributors);
		Log.w("", "new Thread ID in DB=" + String.valueOf(id)); // debug
		if (id != 0)
			return new Thread(id, contributors);
		return null;
	}

	/**
	 * Delete a thread and its messages.
	 * @param ThreadID Thread ID.
	 * @return <code>True</code> if OK.
	 */
	public static boolean deleteThread(int ThreadID) {
		return ThreadDB.deleteThread(ThreadID);
	}

	@Override
	public String toString() {
		return "#" + this.ID + ": " + this.contributors + " (" + this.getNbMsg() + " msg)) - "+this.getLastUpdate();
	}

}
