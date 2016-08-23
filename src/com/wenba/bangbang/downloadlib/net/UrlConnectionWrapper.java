package com.wenba.bangbang.downloadlib.net;

import java.io.InputStream;

import com.wenba.bangbang.downloadlib.DownLoadManager;

/**
 * Created by silvercc on 16/7/11.
 */
public interface UrlConnectionWrapper {
    void setRequestMethod(DownLoadManager.RequsteMethod method);

    int getResponseCode();

    void setDoOutPut(boolean newValue);

    InputStream getInputStream();

    void setConnectTimeout(int timeout);

    void setReadTimeout(int timeout);

    void setRequestProperty(String range, String s);

    void disconnect();

    long getFileSize();
}
