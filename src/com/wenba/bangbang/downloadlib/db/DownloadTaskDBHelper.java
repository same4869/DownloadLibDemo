package com.wenba.bangbang.downloadlib.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.wenba.bangbang.downloadlib.model.DownLoadBean;

/**
 * Created by silvercc on 16/7/7.
 */
public class DownloadTaskDBHelper extends BaseDBHelper<DownLoadBean> {
    private static final String TABLE_NAME = "DOWNLOAD_TASK";
    private static final int MAX_SAVE_TIME = 5;
    private volatile static DownloadTaskDBHelper mInstance;
    private WenbaDatabaseHelper mHelper;
    private int saveTime;

    private DownloadTaskDBHelper(Context context) {
        mHelper = WenbaDatabaseHelper.getInstance(context);
    }

    public static DownloadTaskDBHelper getInstance(Context context) {
        if (mInstance == null) {
            synchronized (DownloadTaskDBHelper.class) {
                if (mInstance == null) {
                    mInstance = new DownloadTaskDBHelper(context);
                }
            }
        }
        return mInstance;
    }

    @Override
    public String getTable() {
        return TABLE_NAME;
    }

    @Override
    public void save(DownLoadBean obj) {
        String sql = "insert into " + getTable() + "(" + "UID,TASK_ID,URL,FILE_PATH,FILE_NAME,FILE_SIZE,DOWNLOAD_SIZE,IS_SUPPORT_BREAKPOINT" + ")values(?,?,?,?,?,?,?,?)";
        Object[] bindArgs = {obj.getUserID(), obj.getTaskID(), obj.getUrl(), obj.getFilePath(), obj.getFileName(), obj.getFileSize(), obj.getDownloadSize(), obj.isSupportBreakpoint() == true ? 1 : 0};
        Cursor cursor = null;
        try {
            cursor = mHelper.rawQuery("SELECT * FROM " + getTable()
                    + " WHERE UID = ? AND TASK_ID = ? ", new String[]{obj.getUserID(), obj.getTaskID()});
            if (cursor.moveToNext()) {
                update(obj);
            } else {
                mHelper.execSQL(sql, bindArgs);
            }
        } catch (Exception e) {
            saveTime++;
            if (saveTime < MAX_SAVE_TIME) {
                save(obj);
            } else {
                saveTime = 0;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        saveTime = 0;
    }

    @Override
    public void update(DownLoadBean obj) {
        String selection = null;
        String[] selectionArgs = null;
        selection = "UID = ? AND TASK_ID = ?";
        selectionArgs = new String[]{obj.getUserID(), obj.getTaskID()};

        ContentValues cv = new ContentValues();
        cv.put("URL", obj.getUrl());
        cv.put("FILE_PATH", obj.getFilePath());
        cv.put("FILE_NAME", obj.getFileName());
        cv.put("FILE_SIZE", obj.getFileSize());
        cv.put("DOWNLOAD_SIZE", obj.getDownloadSize());
        mHelper.update(getTable(), cv, selection, selectionArgs);
    }

    public List<DownLoadBean> findUserDownLoadInfo(String userID) {
        List<DownLoadBean> list = new ArrayList<DownLoadBean>();
        String sql = "SELECT * FROM " + getTable() + " WHERE UID = ?";
        Cursor cursor = null;
        try {
            cursor = mHelper.rawQuery(sql, new String[]{userID});
            while (cursor.moveToNext()) {
                DownLoadBean downloadinfo = new DownLoadBean();
                downloadinfo.setUserID(cursor.getString(cursor.getColumnIndex("UID")));
                downloadinfo.setTaskID(cursor.getString(cursor.getColumnIndex("TASK_ID")));
                downloadinfo.setUrl(cursor.getString(cursor.getColumnIndex("URL")));
                downloadinfo.setFilePath(cursor.getString(cursor.getColumnIndex("FILE_PATH")));
                downloadinfo.setFileName(cursor.getString(cursor.getColumnIndex("FILE_NAME")));
                downloadinfo.setFileSize(cursor.getLong(cursor.getColumnIndex("FILE_SIZE")));
                downloadinfo.setDownloadSize(cursor.getLong(cursor.getColumnIndex("DOWNLOAD_SIZE")));
                downloadinfo.setIsSupportBreakpoint(cursor.getLong(cursor.getColumnIndex("DOWNLOAD_SIZE")) > 0 ? true : false);
                list.add(downloadinfo);
            }
        } catch (Exception e) {
            Log.w("wenba", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    public DownLoadBean findDownLoadInfo(String userID, String taskID) {
        DownLoadBean downloadinfo = null;
        String sql = "SELECT * FROM " + getTable() + " WHERE UID = ? AND TASK_ID = ?";
        Cursor cursor = null;
        try {
            cursor = mHelper.rawQuery(sql, new String[]{userID, taskID});
            while (cursor.moveToNext()) {
                downloadinfo = new DownLoadBean();
                downloadinfo.setUserID(cursor.getString(cursor.getColumnIndex("UID")));
                downloadinfo.setTaskID(cursor.getString(cursor.getColumnIndex("TASK_ID")));
                downloadinfo.setUrl(cursor.getString(cursor.getColumnIndex("URL")));
                downloadinfo.setFilePath(cursor.getString(cursor.getColumnIndex("FILE_PATH")));
                downloadinfo.setFileName(cursor.getString(cursor.getColumnIndex("FILE_NAME")));
                downloadinfo.setFileSize(cursor.getLong(cursor.getColumnIndex("FILE_SIZE")));
                downloadinfo.setDownloadSize(cursor.getLong(cursor.getColumnIndex("DOWNLOAD_SIZE")));
                downloadinfo.setIsSupportBreakpoint(cursor.getLong(cursor.getColumnIndex("DOWNLOAD_SIZE")) > 0 ? true : false);
            }
        } catch (Exception e) {
            Log.w("wenba", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return downloadinfo;
    }

    public List<DownLoadBean> findAllDownLoadInfo() {
        List<DownLoadBean> list = new ArrayList<DownLoadBean>();
        String sql = "SELECT * FROM " + getTable();
        Cursor cursor = null;
        try {
            cursor = mHelper.rawQuery(sql, new String[]{});
            while (cursor.moveToNext()) {
                DownLoadBean downloadinfo = new DownLoadBean();
                downloadinfo.setUserID(cursor.getString(cursor.getColumnIndex("UID")));
                downloadinfo.setTaskID(cursor.getString(cursor.getColumnIndex("TASK_ID")));
                downloadinfo.setUrl(cursor.getString(cursor.getColumnIndex("URL")));
                downloadinfo.setFilePath(cursor.getString(cursor.getColumnIndex("FILE_PATH")));
                downloadinfo.setFileName(cursor.getString(cursor.getColumnIndex("FILE_NAME")));
                downloadinfo.setFileSize(cursor.getLong(cursor.getColumnIndex("FILE_SIZE")));
                downloadinfo.setDownloadSize(cursor.getLong(cursor.getColumnIndex("DOWNLOAD_SIZE")));
                downloadinfo.setIsSupportBreakpoint(cursor.getLong(cursor.getColumnIndex("DOWNLOAD_SIZE")) > 0 ? true : false);
                list.add(downloadinfo);
            }
        } catch (Exception e) {
            Log.w("wenba", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    public void deleteAllDownLoadInfo() {
        String sql = "DELETE FROM " + getTable();
        mHelper.execSQL(sql);
    }

    public void deleteUserDownLoadInfo(String userID) {
        String sql = "DELETE FROM " + getTable() + " WHERE UID = \"" + userID + "\"";
        mHelper.execSQL(sql);
    }

    public void deleteDownLoadInfo(String userID, String taskID) {
        String sql = "DELETE FROM " + getTable() + " WHERE UID = \"" + userID + "\" AND TASK_ID = \"" + taskID + "\"";
        mHelper.execSQL(sql);
    }

    @Override
    public void delete(String id) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public DownLoadBean find(String id) {
        return null;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public List<DownLoadBean> getAllData() {
        return null;
    }
}
