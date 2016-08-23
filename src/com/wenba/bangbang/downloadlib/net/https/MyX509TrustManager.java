package com.wenba.bangbang.downloadlib.net.https;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import android.content.Context;

import com.example.iamademoyet2.R;

/**
 * Created by silvercc on 16/2/29.
 */
public class MyX509TrustManager implements X509TrustManager {
    private X509TrustManager mX509TrustManager;

    public MyX509TrustManager(Context context) {
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null);

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream is = context.getResources().openRawResource(R.raw.xueba100);

            Certificate certificate = cf.generateCertificate(is);
            ks.setCertificateEntry("ibest0", certificate);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);
            TrustManager tms[] = tmf.getTrustManagers();
            for (int i = 0; i < tms.length; i++) {
                if (tms[i] instanceof X509TrustManager) {
                    mX509TrustManager = (X509TrustManager) tms[i];
                    return;
                }
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        mX509TrustManager.checkClientTrusted(chain, authType);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        mX509TrustManager.checkServerTrusted(chain, authType);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return mX509TrustManager.getAcceptedIssuers();
    }
}
