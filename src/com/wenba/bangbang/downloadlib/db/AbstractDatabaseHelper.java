package com.wenba.bangbang.downloadlib.db;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 
 * @Author:Lijj
 * @Date:2014-5-16上午10:44:41 * TODO 数据库帮手基类，一个数据库对应一个具体实现。
 *                           可以在内部定义表名，字段名和各种业务逻辑操作等等。 对数据库的增删改操作，请使用事务管理。
 *                           对数据的查询操作
 *                           ，请在使用Cursor前，调用startManagingCursor()方法管理Cursor。
 */
@SuppressLint("NewApi")
public abstract class AbstractDatabaseHelper {

	/** */
	/**
	 * SQLite数据库实例
	 */
	public SQLiteDatabase mDb = null;

	/** */
	/**
	 * 数据库创建帮手
	 */
	protected CreateDBHelper mDbHelper = null;

	/** */
	/**
	 * 获得当前数据库帮手类标识(一般是该类名称)，用于日志等的记录
	 * 
	 * @return
	 */
	protected abstract String getTag();

	/** */
	/**
	 * 获得数据库名称
	 * 
	 * @return
	 */
	protected abstract String getMyDatabaseName();

	/** */
	/**
	 * 获得数据库版本，值至少为1。 当数据库结构发生改变的时候，请将此值加1，系统会在初始化时自动调用
	 * createDBTables和dropDBTables方法更新数据库结构。
	 * 
	 * @return
	 */
	protected abstract int getDatabaseVersion();

	/** */
	/**
	 * 创建数据库表的SQL语句，一个元素一条语句
	 * 
	 * @return
	 */
	protected abstract String[] createDBTables();

	/** */
	/**
	 * 删除数据库表的SQL语句，一个元素一条语句
	 * 
	 * @return
	 */
	protected abstract String[] dropDBTables();

	/** */
	/**
	 * 
	 * TODO 内部数据库创建帮手类
	 * 
	 */
	private class CreateDBHelper extends SQLiteOpenHelper {
		public CreateDBHelper(Context ctx) {
			super(ctx, getMyDatabaseName(), null, getDatabaseVersion());
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			executeBatch(createDBTables(), db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.e(getTag(), "Upgrading database '" + getMyDatabaseName() + "' from version " + oldVersion + " to "
					+ newVersion);
			for (int i = oldVersion; i < newVersion; i++) {
				if (i <= 14) {
					String[] tasks = { "DROP TABLE IF EXISTS UPLOAD_TASK", "DROP TABLE IF EXISTS FEED_COMMENT",
							"DROP TABLE IF EXISTS SHARE", "DROP TABLE IF EXISTS MESSAGE", "DROP TABLE IF EXISTS CLIPS" };
					executeBatch(tasks, db);
				} else if (i <= 16) {
					String[] updateClipTable = { "ALTER TABLE CLIPS ADD COLUMN FAV_ID VARCHAR(32)",
							"ALTER TABLE CLIPS ADD COLUMN TYPE INTEGER" };
					executeBatch(updateClipTable, db);
				} else if (i <= 18) {
					db.execSQL("DROP TABLE IF EXISTS MESSAGE");
				} else if (i <= 19) {
					db.execSQL("DROP TABLE IF EXISTS DOWNLOAD_TASK");
				}
			}
			onCreate(db);
		}

		/** */
		/**
		 * 批量执行Sql语句
		 * 
		 * @param sqls
		 * @param db
		 */
		private void executeBatch(String[] sqls, SQLiteDatabase db) {
			if (sqls == null)
				return;

			db.beginTransaction();
			try {
				int len = sqls.length;
				for (int i = 0; i < len; i++) {
					db.execSQL(sqls[i]);
				}
				db.setTransactionSuccessful();
			} catch (Exception e) {
				Log.d(getTag(), e.getMessage());
			} finally {
				db.endTransaction();
			}

		}
	}

	/** */
	/**
	 * 打开或者创建一个指定名称的数据库
	 * 
	 * @param dbName
	 * @param ctx
	 */
	private synchronized void open(Context ctx) {
		if (mDbHelper == null) {
			try {
				mDbHelper = new CreateDBHelper(ctx);
				mDb = mDbHelper.getWritableDatabase();
			} catch (Exception e) {
				Log.w("wenba", e);
			}
		}
	}

	protected void init(Context ctx) {
		if (ctx == null) {
			throw new RuntimeException("ctx is null");
		}
		if (mDbHelper == null) {
			open(ctx);
		}
	}

	/** */
	/**
	 * 关闭数据库
	 */
	public void close() {
		try {
			if (mDb != null && mDb.isOpen()) {
				mDb.close();
			}
			if (mDbHelper != null) {
				mDbHelper.close();
			}
		} catch (Exception e) {
			Log.w("wenba", e);
		}
	}

}