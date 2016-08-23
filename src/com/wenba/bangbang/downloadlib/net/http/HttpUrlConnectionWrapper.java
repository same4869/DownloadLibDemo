package com.wenba.bangbang.downloadlib.net.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;

import com.wenba.bangbang.downloadlib.DownLoadManager;
import com.wenba.bangbang.downloadlib.net.UrlConnectionWrapper;

/**
 * Created by silvercc on 16/7/11.
 */
public class HttpUrlConnectionWrapper implements UrlConnectionWrapper{
    private HttpURLConnection mHttpURLConnection;
    public HttpUrlConnectionWrapper(HttpURLConnection connection) {
        mHttpURLConnection = connection;
    }

    @Override
    public void setRequestMethod(DownLoadManager.RequsteMethod method) {
        try {
            if(method == DownLoadManager.RequsteMethod.GET){
                mHttpURLConnection.setRequestMethod("GET");
            } else {
                mHttpURLConnection.setRequestMethod("POST");
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getResponseCode() {
        try {
            return mHttpURLConnection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void setDoOutPut(boolean newValue) {
        mHttpURLConnection.setDoOutput(newValue);
    }

    @Override
    public InputStream getInputStream() {
        try {
            return mHttpURLConnection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setConnectTimeout(int timeout) {
        mHttpURLConnection.setConnectTimeout(timeout);
    }

    @Override
    public void setReadTimeout(int timeout) {
        mHttpURLConnection.setReadTimeout(timeout);
    }

    @Override
    public void setRequestProperty(String range, String s) {
        mHttpURLConnection.setRequestProperty(range, s);
    }

    @Override
    public void disconnect() {
        mHttpURLConnection.disconnect();
    }

    @Override
    public long getFileSize() {
        return mHttpURLConnection.getContentLength();
    }
}
