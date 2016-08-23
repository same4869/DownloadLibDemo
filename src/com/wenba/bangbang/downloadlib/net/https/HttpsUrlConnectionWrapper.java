package com.wenba.bangbang.downloadlib.net.https;

import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import com.wenba.bangbang.downloadlib.DownLoadManager;
import com.wenba.bangbang.downloadlib.net.UrlConnectionWrapper;

/**
 * Created by silvercc on 16/2/28.
 */
public class HttpsUrlConnectionWrapper<T extends TrustManager, K extends KeyManager, H extends HostnameVerifier> implements UrlConnectionWrapper {
    private HttpsURLConnection mHttpsURLConnection;
    private T mTrustManager;
    private K mKeyManager;
    private H mHostnameVerifier;

    public HttpsUrlConnectionWrapper(HttpsURLConnection httpsURLConnection, T t, K k, H h) {
        mHttpsURLConnection = httpsURLConnection;
        mTrustManager = t;
        mKeyManager = k;
        mHostnameVerifier = h;
        init();
    }

    public HttpsURLConnection getHttpsURLConnection() {
        return mHttpsURLConnection;
    }

    private void init() {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");
            TrustManager[] tmArray = new TrustManager[]{mTrustManager};
            KeyManager[] kmArray = new KeyManager[]{mKeyManager};
            sslContext.init(kmArray, tmArray, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        if (sslContext != null) {
            mHttpsURLConnection.setSSLSocketFactory(sslContext.getSocketFactory());
        }
        if (mHostnameVerifier != null) {
            mHttpsURLConnection.setHostnameVerifier(mHostnameVerifier);
        }
    }

    @Override
    public void setRequestMethod(DownLoadManager.RequsteMethod method) {
        try {
            if (method == DownLoadManager.RequsteMethod.POST) {
                mHttpsURLConnection.setRequestMethod("POST");

            } else {
                mHttpsURLConnection.setRequestMethod("GET");
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getResponseCode() {
        try {
            return mHttpsURLConnection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void setDoOutPut(boolean newValue) {
        mHttpsURLConnection.setDoOutput(newValue);
    }

    @Override
    public InputStream getInputStream() {
        try {
            return mHttpsURLConnection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setConnectTimeout(int timeout) {
        mHttpsURLConnection.setConnectTimeout(timeout);
    }

    @Override
    public void setReadTimeout(int timeout) {
        mHttpsURLConnection.setReadTimeout(timeout);
    }

    @Override
    public void setRequestProperty(String range, String s) {
        mHttpsURLConnection.setRequestProperty(range, s);
    }

    @Override
    public void disconnect() {
        mHttpsURLConnection.disconnect();
    }

    @Override
    public long getFileSize() {
        return mHttpsURLConnection.getContentLength();
    }
}
