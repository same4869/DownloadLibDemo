package com.example.iamademoyet2;

import com.wenba.bangbang.downloadlib.DownLoadCallBack;
import com.wenba.bangbang.downloadlib.DownLoadManager;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener, DownLoadCallBack {
	private Button startDownloadBtn;
	private Button puaseButton;
	private String upgradeTaskId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initView();
		initData();
	}

	private void initData() {
		String appUrl = "http://music.baidu.com/cms/BaiduMusic-pcwebdownload.apk";
		String downloadPath = Environment.getExternalStorageDirectory() + "/BaiduMusic-pcwebdownload.apk";
		upgradeTaskId = DownLoadManager.getInstance(getApplicationContext()).addTask(null, appUrl, downloadPath, true,
				this);
		Log.d("kkkkkkkk", "initData upgradeTaskId --> " + upgradeTaskId);

	}

	private void initView() {
		startDownloadBtn = (Button) findViewById(R.id.start_downlaod);
		startDownloadBtn.setOnClickListener(this);
		puaseButton = (Button) findViewById(R.id.pause_downlaod);
		puaseButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.start_downlaod:
			DownLoadManager.getInstance(getApplicationContext()).startTask(upgradeTaskId);
			break;
		case R.id.pause_downlaod:
			DownLoadManager.getInstance(getApplicationContext()).stopTask(upgradeTaskId);
			break;

		default:
			break;
		}

	}

	@Override
	public void onStart(String taskID) {
		Log.d("kkkkkkkk", "onStart taskID --> " + taskID);
	}

	@Override
	public void onLoading(long total, long current) {
		int percent = (int) (current * 100 / total);
		Log.d("kkkkkkkk", "onLoading percent --> " + percent);
	}

	@Override
	public void onSuccess(String path, String taskID) {
		Log.d("kkkkkkkk", "onSuccess path --> " + path + " taskID --> " + taskID);
	}

	@Override
	public void onFailure(String msg) {
		Log.d("kkkkkkkk", "onFailure msg --> " + msg);
	}

	@Override
	public void onStop(long total, long current, String taskID) {
		int percent = (int) (current * 100 / total);
		Log.d("kkkkkkkk", "onStop percent --> " + percent + " taskID --> " + taskID);
	}

}
