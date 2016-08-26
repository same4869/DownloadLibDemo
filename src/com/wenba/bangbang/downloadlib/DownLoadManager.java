package com.wenba.bangbang.downloadlib;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.R.integer;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.util.Log;

import com.wenba.bangbang.downloadlib.db.DownloadTaskDBHelper;
import com.wenba.bangbang.downloadlib.model.DownLoadBean;
import com.wenba.bangbang.downloadlib.util.StringUtil;

/**
 * Created by silvercc on 16/7/7.
 */
public class DownLoadManager {
	public static final String TAG = "download";

	private static final int CORE_POOL_SIZE = 5;
	private static final int MAXIUM_POOL_SIZE = 10;
	private static final int KEEP_LIVE_TIME = 30;
	private static final int QUEUE_SIZE = 2000;
	private static DownLoadManager mInstance;
	private ThreadPoolExecutor mPool;
	private HashMap<String, DownLoader> mDownLoaderList = new HashMap<String, DownLoader>();
	private Context mContext;
	private NetStateReceiver mNetStateBroadCastReceiver;

	private DownLoadManager(Context context) {
		mContext = context;
		mPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIUM_POOL_SIZE, KEEP_LIVE_TIME, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(QUEUE_SIZE));
		// recoverFromDb(context, "12345");
	}

	public static DownLoadManager getInstance(Context context) {
		if (mInstance == null) {
			synchronized (DownLoadManager.class) {
				if (mInstance == null) {
					mInstance = new DownLoadManager(context);
				}
			}
		}
		return mInstance;
	}

	private void recoverFromDb(Context context, String userID) {
		DownloadTaskDBHelper helper = DownloadTaskDBHelper.getInstance(context);
		mDownLoaderList = new HashMap<String, DownLoader>();
		List<DownLoadBean> downLoadInfos = null;
		if (TextUtils.isEmpty(userID)) {
			downLoadInfos = helper.findAllDownLoadInfo();
		} else {
			downLoadInfos = helper.findUserDownLoadInfo(userID);
		}
		if (downLoadInfos.size() > 0) {
			for (int i = 0; i < downLoadInfos.size(); i++) {
				if (downLoadInfos.get(i).getFileSize() > 0) {
					DownLoader loader = new DownLoader(context, downLoadInfos.get(i), mPool, false);
					mDownLoaderList.put(downLoadInfos.get(i).getTaskID(), loader);
				}
			}
		}
	}

	public boolean startTask(String taskId) {
		if (mDownLoaderList.containsKey(taskId)) {
			DownLoader downLoader = mDownLoaderList.get(taskId);
			if (downLoader != null && !downLoader.isDownLoading()) {
				downLoader.start();
				return true;
			}
		}
		return false;
	}

	public boolean stopTask(String taskId) {
		if (mDownLoaderList.containsKey(taskId)) {
			DownLoader downLoader = mDownLoaderList.get(taskId);
			if (downLoader != null && downLoader.isDownLoading()) {
				downLoader.stop();
				return true;
			}
		}
		return false;
	}

	public void startAllTask() {
		Set<Entry<String, DownLoader>> set = mDownLoaderList.entrySet();
		Iterator<Entry<String, DownLoader>> iterator = set.iterator();
		while (iterator.hasNext()) {
			Entry<String, DownLoader> entry = iterator.next();
			DownLoader downLoader = entry.getValue();
			if (downLoader != null && !downLoader.isDownLoading()) {
				downLoader.start();
			}
		}
	}

	public void stopAllTask() {
		Set<Entry<String, DownLoader>> set = mDownLoaderList.entrySet();
		Iterator<Entry<String, DownLoader>> iterator = set.iterator();
		while (iterator.hasNext()) {
			Entry<String, DownLoader> entry = iterator.next();
			DownLoader downLoader = entry.getValue();
			if (downLoader != null && downLoader.isDownLoading()) {
				downLoader.stop();
			}
		}
		unRegistReceiver();
	}

	private String getTaskId(String uid, String url, String path) {
		return StringUtil.md5(uid + url + path);
	}

	public String addTask(String uid, String url, String path, boolean isSupportBreakpoint, DownLoadCallBack callBack) {
		Log.d(TAG, "url=" + url);
		Log.d(TAG, "path=" + path);
		String taskId = getTaskId(uid, url, path);

		Log.d(TAG, "taskId=" + taskId);

		DownLoader downLoader = findTask(uid, taskId, true);
		if (downLoader == null) {
			String fileName = path.substring(path.lastIndexOf("/") + 1);
			String filePath = path.substring(0, path.lastIndexOf("/"));

			Log.d(TAG, "fileName=" + fileName);
			Log.d(TAG, "filePath=" + filePath);

			DownLoadBean info = new DownLoadBean();
			if (TextUtils.isEmpty(uid)) {
				uid = "12345";// UserManager.getCurUserId();
			}

			info.setUserID(uid);
			info.setTaskID(taskId);
			info.setFilePath(filePath);
			info.setFileName(fileName);
			info.setIsSupportBreakpoint(isSupportBreakpoint);
			info.setUrl(url);

			downLoader = new DownLoader(mContext, info, mPool, true);
			mDownLoaderList.put(taskId, downLoader);
		} else {
			if (!mDownLoaderList.containsKey(taskId)) {
				mDownLoaderList.put(taskId, downLoader);
			}
		}
		registReceiver();
		downLoader.setDownLoadCallBack(callBack);

		if (!downLoader.isDownLoading()) {
			downLoader.start();
		}

		return taskId;
	}

	public void deleteTask(String taskID) {
		DownLoader loader = findTask("12345", taskID, true);
		if (loader != null) {
			loader.destroy();
		}
		mDownLoaderList.remove(taskID);
		unRegistReceiver();
	}

	public void deleteAllTask() {
		Set<Entry<String, DownLoader>> set = mDownLoaderList.entrySet();
		Iterator<Entry<String, DownLoader>> iterator = set.iterator();
		while (iterator.hasNext()) {
			Entry<String, DownLoader> entry = iterator.next();
			DownLoader downLoader = entry.getValue();
			if (downLoader != null) {
				downLoader.destroy();
			}
		}
		mDownLoaderList.clear();
		unRegistReceiver();
	}

	private DownLoader findTask(String uid, String taskID, boolean fromDB) {
		if (taskID == null) {
			return null;
		}
		DownLoader downLoader = null;
		if (mDownLoaderList.containsKey(taskID)) {
			downLoader = mDownLoaderList.get(taskID);
		}

		if (downLoader == null && fromDB) {
			DownloadTaskDBHelper helper = DownloadTaskDBHelper.getInstance(mContext);
			DownLoadBean downLoadInfo = helper.find(uid, taskID);
			if (downLoadInfo != null) {
				downLoader = new DownLoader(mContext, downLoadInfo, mPool, false);
			}
		}
		return downLoader;
	}

	public void finishCallback(String taskID) {
		DownLoader downLoader = findTask("12345", taskID, false);
		if (downLoader != null) {
			downLoader.removeDownLoadListener();
		}
	}

	private void registReceiver() {
		if (mNetStateBroadCastReceiver == null) {
			mNetStateBroadCastReceiver = new NetStateReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			mContext.registerReceiver(mNetStateBroadCastReceiver, filter);
		}
	}

	private void unRegistReceiver() {
		if (mDownLoaderList.isEmpty() && mNetStateBroadCastReceiver != null) {
			mContext.unregisterReceiver(mNetStateBroadCastReceiver);
			mNetStateBroadCastReceiver = null;
		}
	}

	public enum RequsteMethod {
		GET, POST
	}
}
