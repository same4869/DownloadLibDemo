package com.wenba.bangbang.downloadlib.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

/**
 * 
 * @Author:Lijj
 * @Date:2014-5-16上午10:44:01
 * @Todo:TODO
 */
public class WenbaDatabaseHelper extends AbstractDatabaseHelper {
	private static WenbaDatabaseHelper instance = null;

	private String databaseName = "Wenba.db";
	private String tag = "Wenba_database";
	private int databaseVersion = 20;
	private Context context;

	@Override
	protected String[] createDBTables() {
		String[] object = {
				"CREATE TABLE IF NOT EXISTS UPLOAD_TASK(" + "ID INTEGER PRIMARY KEY AUTOINCREMENT" + ",UID VARCHAR(32)"
						+ ",TASK_ID VARCHAR(100)" + ",CREATE_TIME TIMESTAMP" + ",STATUS VARHCHAR(20)"
						+ ",TAKS_BEAN BLOB" + ")",

				"CREATE TABLE IF NOT EXISTS FEED_DETAIL(" + "ID INTEGER PRIMARY KEY AUTOINCREMENT"
						+ ",FEED_ID VARCHAR(100)" + ",UID VARCHAR(32)" + ",FEED_BEAN BLOB" + ")",

				"CREATE TABLE IF NOT EXISTS SETTING(" + "ID INTEGER PRIMARY KEY AUTOINCREMENT" + ",_KEY VARCHAR(100)"
						+ ",_VALUE VARCHAR(100)" + ")",

				"CREATE TABLE IF NOT EXISTS FEED_COLLECT(" + "ID INTEGER PRIMARY KEY AUTOINCREMENT"
						+ ",UID VARCHAR(32)" + ",FAV_ID VARCHAR(32)" + ",AID VARCHAR(32)" + ",FEED_ID VARCHAR(100)"
						+ ",SUBJECT VARCHAR(8)" + ",FEED_COLLECT BLOB" + ")",

				"CREATE TABLE IF NOT EXISTS FEED_COMMENT(" + "ID INTEGER PRIMARY KEY AUTOINCREMENT"
						+ ",SID VARCHAR(32)" + ",UID VARCHAR(32)" + ",AID VARCHAR(32)" + ",CREATE_TIME TIMESTAMP"
						+ ",FEED_COMMENT BLOB" + ")",

				"CREATE TABLE IF NOT EXISTS SHARE(" + "ID INTEGER PRIMARY KEY AUTOINCREMENT" + ",UID VARCHAR(32)"
						+ ",AID VARCHAR(32)" + ",SID VARCHAR(32)" + ",TYPE INTEGER" + ",CREATE_TIME TIMESTAMP"
						+ ",SUBJECT VARCHAR(8)" + ",SHARE_BEAN BLOB" + ")",

				"CREATE TABLE IF NOT EXISTS MESSAGE(" + "ID INTEGER PRIMARY KEY AUTOINCREMENT" + ",CATEGORY INTEGER"
						+ ",MESSAGE_ID VARCHAR(32)" + ",UID VARCHAR(32)" + ",STATUS INTEGER" + ",CREATE_TIME TIMESTAMP"
						+ ",MESSAGE_BEAN BLOB" + ")",

				"CREATE TABLE IF NOT EXISTS CLIPS(" + "ID INTEGER PRIMARY KEY AUTOINCREMENT" + ",UID VARCHAR(32)"
						+ ",ARTICLE_ID VARCHAR(32)" + ",ARTICLE_INDEX INTEGER" + ",FAV_ID VARCHAR(32)"
						+ ",TYPE INTEGER" + ",CREATE_TIME TIMESTAMP" + ",CLIPS_BEAN BLOB" + ")",

				"CREATE TABLE IF NOT EXISTS DOWNLOAD_TASK( " + "ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL"
						+ ",UID VARCHAR" + ",TASK_ID VARCHAR" + ",URL VARCHAR" + ",FILE_PATH VARCHAR"
						+ ",FILE_NAME VARCHAR" + ",FILE_SIZE INTEGER" + ",DOWNLOAD_SIZE INTEGER"
						+ ",IS_SUPPORT_BREAKPOINT INTEGER" + ")",
				"CREATE TABLE IF NOT EXISTS TEST_CENTER( " + "ID INTEGER PRIMARY KEY AUTOINCREMENT"
						+ ",UID VARCHAR(32)" + ",CENTER_ID VARCHAR(100)" + ",SUBJECT VARCHAR(32)" + ",CENTER_BEAN BLOB"
						+ ")" };
		return object;
	}

	@Override
	protected String[] dropDBTables() {
		String[] object = { "DROP TABLE IF EXISTS UPLOAD_TASK", "DROP TABLE IF EXISTS FEED_DETAIL",
				"DROP TABLE IF EXISTS SETTING", "DROP TABLE IF EXISTS FEED_COLLECT",
				"DROP TABLE IF EXISTS FEED_COMMENT", "DROP TABLE IF EXISTS SHARE", "DROP TABLE IF EXISTS MESSAGE",
				"DROP TABLE IF EXISTS CLIPS", "DROP TABLE IF EXISTS DOWNLOAD_TASK", "DROP TABLE IF EXISTS CLIPS",
				"DROP TABLE IF EXISTS TEST_CENTER" };
		return object;
	}

	@Override
	protected String getMyDatabaseName() {
		return databaseName;
	}

	@Override
	protected int getDatabaseVersion() {
		return databaseVersion;
	}

	@Override
	protected String getTag() {
		return tag;
	}

	private static synchronized void initSyn(Context context) {
		instance = new WenbaDatabaseHelper(context);
	}

	public static WenbaDatabaseHelper getInstance(Context context) {
		if (instance == null) {
			initSyn(context);
		}
		return instance;
	}

	private WenbaDatabaseHelper(Context context) {
		this.context = context;
	}

	public void execSQL(String sql, Object[] bindArgs) {
		init(context);
		if (mDb == null) {
			return;
		}
		try {
			mDb.execSQL(sql, bindArgs);
		} catch (Exception e) {
			Log.w("wenba", e);
		}
	}

	public void execSQL(String[] sql, Object[][] bindArgs) {
		if (sql == null || sql.length == 0) {
			return;
		}
		init(context);
		if (mDb == null) {
			return;
		}
		for (int i = 0; i < sql.length; i++) {
			try {
				mDb.execSQL(sql[i], bindArgs[i]);
			} catch (Exception e) {
				Log.w("wenba", e);
			}
		}
	}

	public void execSQL(String sql) {
		init(context);
		if (mDb == null) {
			return;
		}
		try {
			mDb.execSQL(sql);
		} catch (Exception e) {
			Log.w("wenba", e);
		}
	}

	public void update(String table, ContentValues values, String whereClause, String[] whereArgs) {
		init(context);
		if (mDb == null) {
			return;
		}
		try {
			mDb.update(table, values, whereClause, whereArgs);
		} catch (Exception e) {
			Log.w("wenba", e);
		}
	}

	public Cursor rawQuery(String sql, String[] selectionArgs) {
		init(context);
		if (mDb == null) {
			return null;
		}
		return mDb.rawQuery(sql, selectionArgs);
	}

	public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy,
			String having, String orderBy) {
		init(context);

		if (mDb == null) {
			return null;
		}
		return mDb.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
	}
}
