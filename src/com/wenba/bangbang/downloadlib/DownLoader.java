package com.wenba.bangbang.downloadlib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.ThreadPoolExecutor;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.wenba.bangbang.downloadlib.db.DownloadTaskDBHelper;
import com.wenba.bangbang.downloadlib.model.DownLoadBean;
import com.wenba.bangbang.downloadlib.net.ConnectionWrapperManager;

/**
 * Created by silvercc on 16/7/7.
 */
public class DownLoader {
	private static final int CONNECT_TIME_OUT = 5000;
	private static final int READ_TIME_OUT = 10000;
	private static final String ERROR_KEY = "error_key";
	private static final String STOP_KEY = "size";

	private int TASK_START = 0;
	private int TASK_STOP = 1;
	private int TASK_PROGESS = 2;
	private int TASK_ERROR = 3;
	private int TASK_SUCCESS = 4;

	private DownloadTaskDBHelper mHelper;
	private String mUrl;
	private long mFileSize;
	private long mDownLoadSize;
	private Context mContext;
	private DownLoadBean mSQLDownLoadInfo;
	private ThreadPoolExecutor mPool;
	private boolean mIsSupportBreakpoint;
	private DownLoadTask mDownLoadTask;
	private RandomAccessFile mLocalFile;
	private String mFilePath;
	private String mFileName;
	private DownLoadCallBack mDownLoadCallBack;
	private long mSafeDownLoadSize;
	/**
	 * 当前任务的状态
	 */
	private boolean ondownload;
	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (mDownLoadCallBack != null) {
				if (msg.what == TASK_START) { // 开始下载
					mDownLoadCallBack.onStart(getTaskID());
				} else if (msg.what == TASK_STOP) { // 停止下载
					mDownLoadCallBack.onStop(mFileSize, mDownLoadSize, getTaskID());
					if (mDownLoadSize == 0) {
						mDownLoadCallBack = null;
					}
				} else if (msg.what == TASK_PROGESS) { // 改变进程
					mDownLoadCallBack.onLoading(mFileSize, mDownLoadSize);
				} else if (msg.what == TASK_ERROR) { // 下载出错
					String error_msg = msg.getData().getString(ERROR_KEY);
					mDownLoadCallBack.onFailure(error_msg);
				} else if (msg.what == TASK_SUCCESS) { // 下载完成
					mDownLoadCallBack.onSuccess(mFilePath + File.separator + mFileName, getTaskID());
					mDownLoadCallBack = null;
				}
			}
		}
	};

	public DownLoader(Context context, DownLoadBean sqlFileInfo, ThreadPoolExecutor pool, boolean isNewTask) {
		mContext = context;
		mSQLDownLoadInfo = sqlFileInfo;
		mPool = pool;
		mIsSupportBreakpoint = sqlFileInfo.isSupportBreakpoint();
		mUrl = mSQLDownLoadInfo.getUrl();
		mFileSize = mSQLDownLoadInfo.getFileSize();
		mDownLoadSize = mSQLDownLoadInfo.getDownloadSize();
		mFilePath = mSQLDownLoadInfo.getFilePath();
		mFileName = mSQLDownLoadInfo.getFileName();
		mHelper = DownloadTaskDBHelper.getInstance(context);
		if (isNewTask) {
			mHelper.save(sqlFileInfo);
		}
	}

	public void setDownLoadCallBack(DownLoadCallBack callBack) {
		mDownLoadCallBack = callBack;
	}

	public String getTaskID() {
		return mSQLDownLoadInfo.getTaskID();
	}

	public void start() {
		if (mDownLoadTask == null) {
			mDownLoadTask = new DownLoadTask();
		}
		mPool.execute(mDownLoadTask);
		ondownload = true;
		mHandler.sendEmptyMessage(TASK_START);
	}

	public void stop() {
		if (mDownLoadTask != null) {
			ondownload = false;
			mDownLoadTask.stopDownLoad();
			mPool.remove(mDownLoadTask);
			mDownLoadTask = null;
		}
	}

	public void destroy() {
		if (mDownLoadTask != null) {
			mDownLoadTask.stopDownLoad();
			mPool.remove(mDownLoadTask);
			mDownLoadTask = null;
		}

		mHelper.deleteDownLoadInfo(mSQLDownLoadInfo.getUserID(), getTaskID());
		File file = new File(mFilePath + File.separator + mFileName + ".tmp");
		if (file.exists()) {
			file.delete();
		}
		mDownLoadSize = 0;
		Message msg = Message.obtain();
		msg.what = TASK_STOP;
		Bundle bundle = new Bundle();
		bundle.putLong(STOP_KEY, mDownLoadSize);
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	private class DownLoadTask implements Runnable {
		private boolean isdownloading;

		public DownLoadTask() {
			isdownloading = true;
		}

		@Override
		public void run() {
			downLoad();
		}

		private void downLoad() {
			if (mDownLoadSize == mFileSize && mFileSize > 0) {// 如果下好的文件大小与文件本身大小一样，则默认已经下载完成，不再下载
				ondownload = false;
				mHandler.sendEmptyMessage(TASK_PROGESS);
				mDownLoadTask = null;
				return;
			}
			ConnectionWrapperManager manager = null;
			InputStream inputStream = null;
			try {
				manager = new ConnectionWrapperManager(mContext, mUrl);
				manager.setRequestMethod(DownLoadManager.RequsteMethod.GET);
				manager.setConnectTimeout(CONNECT_TIME_OUT);
				manager.setReadTimeout(READ_TIME_OUT);
				if (mDownLoadSize < 1) {// 第一次下载，初始化
					openConnention(manager);
				} else {
					if (new File(mFilePath + File.separator + mFileName + ".tmp").exists()) {
						mLocalFile = new RandomAccessFile(mFilePath + File.separator + mFileName + ".tmp", "rwd");
						mLocalFile.seek(mDownLoadSize);
						manager.setRequestProperty("Range", "bytes=" + mDownLoadSize + "-");
					} else {
						mFileSize = 0;
						mDownLoadSize = 0;
						openConnention(manager);
						saveDownloadInfo();
					}
				}
				inputStream = manager.getInputStream();
				byte[] buffer = new byte[1024 * 4];
				int length = -1;
				if (inputStream != null) {
					while ((length = inputStream.read(buffer)) != -1 && isdownloading) {
						mLocalFile.write(buffer, 0, length);
						mDownLoadSize += length;
						mSafeDownLoadSize = mDownLoadSize;
						mHandler.sendEmptyMessage(TASK_PROGESS);
					}
				}
				// 下载完了
				if (mDownLoadSize == mFileSize) {
					boolean renameResult = renameFile();
					if (renameResult) {
						mHandler.sendEmptyMessage(TASK_SUCCESS); // 转移文件成功
					} else {
						new File(mFilePath + File.separator + mFileName + ".tmp").delete();
						Message msg = Message.obtain();
						msg.what = TASK_ERROR;
						Bundle bundle = new Bundle();
						bundle.putString(ERROR_KEY, "转移文件失败");
						msg.setData(bundle);
						mHandler.sendEmptyMessage(TASK_ERROR);// 转移文件失败
					}
					// 清除数据库任务
					mHelper.deleteDownLoadInfo(mSQLDownLoadInfo.getUserID(), getTaskID());
					mDownLoadTask = null;
					mDownLoadCallBack = null;
					ondownload = false;
					mDownLoadSize = 0;
					mSafeDownLoadSize = 0;
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (isdownloading) {
					if (mIsSupportBreakpoint) {
						try {
							if (mLocalFile != null) {
								long length = mLocalFile.length();
								if (length > 0) {
									saveDownloadInfo();
								}
							}
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					ondownload = false;
					mPool.remove(mDownLoadTask);
					mDownLoadTask.stopDownLoad();
					mDownLoadTask = null;
					Message msg = Message.obtain();
					msg.what = TASK_ERROR;
					Bundle bundle = new Bundle();
					bundle.putString(ERROR_KEY, e.getMessage());
					msg.setData(bundle);
					mHandler.sendMessage(msg);
				}
			} finally {
				if (manager != null) {
					manager.disconnect();
				}
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (mLocalFile != null) {
					try {
						mLocalFile.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		private void stopDownLoad() {
			isdownloading = false;

			if (mLocalFile != null) {
				long length = mSafeDownLoadSize;
				if (length > 0) {
					if (mIsSupportBreakpoint) {
						saveDownloadInfo();
					} else {
						clearDownLoadInfo();
					}
				}
			}
			Message msg = Message.obtain();
			msg.what = TASK_STOP;
			Bundle bundle = new Bundle();
			bundle.putLong(STOP_KEY, mDownLoadSize);
			msg.setData(bundle);
			mHandler.sendMessage(msg);
		}

		private void openConnention(ConnectionWrapperManager manager) {
			try {
				if (mFileSize == 0) {
					mFileSize = manager.getFileSize();
				}
				if (mFileSize > 0) {
					isFolderExist();
					mLocalFile = new RandomAccessFile(new File(mFilePath + File.separator + mFileName + ".tmp"), "rwd");
					mSQLDownLoadInfo.setDownloadSize(mFileSize);
					if (isdownloading) {
						saveDownloadInfo();
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 当前任务进行的状态
	 * 
	 * @return
	 */
	public boolean isDownLoading() {
		return ondownload;
	}

	private boolean isFolderExist() {
		boolean result = false;
		try {
			String filepath = mFilePath;
			File file = new File(filepath);
			if (!file.exists()) {
				if (file.mkdirs()) {
					result = true;
				}
			} else {
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 是否支持断点续传就在于中断下载是否保存下载信息
	 */
	private void saveDownloadInfo() {
		if (mIsSupportBreakpoint) {
			mSQLDownLoadInfo.setDownloadSize(mSafeDownLoadSize);
			mHelper.save(mSQLDownLoadInfo);
		}
	}

	private void clearDownLoadInfo() {
		mDownLoadSize = 0;
		mDownLoadCallBack = null;
		mHelper.deleteDownLoadInfo(mSQLDownLoadInfo.getUserID(), getTaskID());
		File file = new File(mFilePath + File.separator + mFileName + ".tmp");
		if (file.exists()) {
			file.delete();
		}
	}

	private boolean renameFile() {
		File newfile = new File(mFilePath + File.separator + mFileName);
		if (newfile.exists()) {
			newfile.delete();
		}
		File oldFile = new File(mFilePath + File.separator + mFileName + ".tmp");
		return oldFile.renameTo(newfile);
	}

	public void removeDownLoadListener() {
		mDownLoadCallBack = null;
	}

}
