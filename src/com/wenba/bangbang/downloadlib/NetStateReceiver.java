package com.wenba.bangbang.downloadlib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by silvercc on 16/7/14.
 */
public class NetStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null) {
            if (networkInfo.isAvailable()) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    DownLoadManager.getInstance(context).startAllTask();
                } else {
                    DownLoadManager.getInstance(context).stopAllTask();
                }
            } else {
                DownLoadManager.getInstance(context).stopAllTask();
            }
        } else {
            DownLoadManager.getInstance(context).stopAllTask();
        }
    }
}
