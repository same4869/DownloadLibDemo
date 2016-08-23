package com.wenba.bangbang.downloadlib.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.X509KeyManager;

import android.content.Context;

import com.wenba.bangbang.downloadlib.DownLoadManager;
import com.wenba.bangbang.downloadlib.net.http.HttpUrlConnectionWrapper;
import com.wenba.bangbang.downloadlib.net.https.HttpsUrlConnectionWrapper;
import com.wenba.bangbang.downloadlib.net.https.MyX509TrustManager;

/**
 * Created by silvercc on 16/7/11.
 */
public class ConnectionWrapperManager implements UrlConnectionWrapper{
    private UrlConnectionWrapper mWrapper;
    public ConnectionWrapperManager(Context context, String murl) {
        try {
            URL url = new URL(murl);
            if (murl.startsWith("https")){
                HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
                mWrapper = new HttpsUrlConnectionWrapper<MyX509TrustManager, X509KeyManager, HostnameVerifier>(conn, new MyX509TrustManager(context), null, null);
            } else if(murl.startsWith("http")){
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                mWrapper = new HttpUrlConnectionWrapper(conn);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setRequestMethod(DownLoadManager.RequsteMethod method) {
        mWrapper.setRequestMethod(method);
    }

    @Override
    public int getResponseCode() {
        return mWrapper.getResponseCode();
    }

    @Override
    public void setDoOutPut(boolean newValue) {
        mWrapper.setDoOutPut(newValue);
    }

    @Override
    public InputStream getInputStream() {
        return mWrapper.getInputStream();
    }

    @Override
    public void setConnectTimeout(int timeout) {
        mWrapper.setConnectTimeout(timeout);
    }

    @Override
    public void setReadTimeout(int timeout) {
        mWrapper.setReadTimeout(timeout);
    }

    @Override
    public void setRequestProperty(String range, String s) {
        mWrapper.setRequestProperty(range, s);
    }

    @Override
    public void disconnect() {
        mWrapper.disconnect();
    }

    @Override
    public long getFileSize() {
        return mWrapper.getFileSize();
    }
}
