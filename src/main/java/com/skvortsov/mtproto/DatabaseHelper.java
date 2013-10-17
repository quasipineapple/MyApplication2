package com.skvortsov.mtproto;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	/** The name of the database file on the file system */
	private static final String DATABASE_NAME = "mtproto";
	/** The version of the database that this class understands. */
	private static final int DATABASE_VERSION = 1;
	private final Context mContext;
	private static final String LOG_TAG = "DatabaseHelper";

	/** Constructor */
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.mContext = context;
	}

	/** Called when it is time to create the database */
	public void onCreate(SQLiteDatabase db) {
		String[] sql = mContext.getString(R.string.Database_onCreate)
				.split("\n");
		db.beginTransaction();
		try {
			// Create tables and test data
			execMultipleSQL(db, sql);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e("Error creating tables and debug data", e.toString());
			Log.v(LOG_TAG,e.toString());
			throw e;
		} finally {
			db.endTransaction();
		}
	}

	/** Called when the database must be upgraded */
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");

		String[] sql = mContext.getString(R.string.Database_onUpgrade)
				.split("\n");
		db.beginTransaction();
		try {
			execMultipleSQL(db, sql);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e("Error upgrading tables and debug data", e.toString());
			throw e;
		} finally {
			db.endTransaction();
		}

		onCreate(db);

	}

	/**
	 * Execute all of the SQL statements in the String[] array
	 * 
	 * @param db
	 *            The database on which to execute the statements
	 * @param sql
	 *            An array of SQL statements to execute
	 */
	private void execMultipleSQL(SQLiteDatabase db, String[] sql) {
		for (String s : sql)
			if (s.trim().length() > 0){
				Log.v(LOG_TAG,s);
				db.execSQL(s);
			}
	}

	/**
	 * Execute the SQL statements to insert data
	 * 
	 * @param sql
	 *            An SQL statements to execute
	 */
	public void insert(String sql) {
		getWritableDatabase().execSQL(sql);
		
		Log.v(LOG_TAG, sql);
	}
	
	/**
	 * Execute the SQL statements to update data
	 * 
	 * @param sql
	 *            An SQL statements to execute
	 */
	public void update(String sql) {
		getWritableDatabase().execSQL(sql);
		Log.v(LOG_TAG, sql);
	}
	
	/**
	 * Execute the SQL statements to delete data
	 * 
	 * @param sql
	 *            An SQL statements to execute
	 */
	public void delete(String sql) {
		getWritableDatabase().execSQL(sql);
		Log.v(LOG_TAG, sql);
	}

	
	/**
	 * Check if data exist in table
	 * 
	 * @param table
	 *            The table to check data
	 * @param whereArgs
	 *            The conditions to check the data
	 * @return boolean
	 * 			  Return True while data exist; False while data not exist 
	 */
	public boolean isDataExist(String table, String[] whereArgs){
		boolean result = false;

		String sql = "select * from " + table + " ";
		String where = "";
		if(null != whereArgs && whereArgs.length>0){
			int j = whereArgs.length;
			for (int i = 0; i < j; i++) {
				where = where.equals("") ? "where " + whereArgs[i] : where
						+ " and" + whereArgs[i];
			}
		}
		
		sql = sql + where;
		
		Log.v(LOG_TAG, sql);

		Cursor cursor = getWritableDatabase().rawQuery(sql, null);

		if (cursor.getCount() > 0) {
			result = true;
		}
		
		cursor.close();

		return result;
	}
	
	/**
	 * Check whether the table exists
	 * 
	 * @param tablename
	 *            The table to check
	 * @return boolean
	 * 			  Return True while table exist; False while table not exist 
	 */
	public boolean isTableExist(String tablename) {
		boolean result = false;
		String sql = "select count(*) xcount  from sqlite_master where table=��"
				+ tablename + "��";
		Log.v(LOG_TAG, sql);
		Cursor c = getWritableDatabase().rawQuery(sql, null);
		int xcount = c.getColumnIndex("xcount");
		if (xcount != 0) {
			result = true;
		}
		c.close();
		
		return result;
	}
}
