package com.wenba.bangbang.downloadlib;

/**
 * Created by silvercc on 16/7/7.
 */
public interface DownLoadCallBack {
    void onStart(String taskID);
    void onLoading(long total, long current);
    void onSuccess(String path, String taskID);
    void onFailure(String msg);
    void onStop(long total, long current, String taskID);
}
