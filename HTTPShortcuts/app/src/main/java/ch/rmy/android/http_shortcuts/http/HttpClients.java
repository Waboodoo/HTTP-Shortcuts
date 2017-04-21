package ch.rmy.android.http_shortcuts.http;


import android.support.annotation.Nullable;

import com.burgstaller.okhttp.digest.Credentials;
import com.burgstaller.okhttp.digest.DigestAuthenticator;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

class HttpClients {

    static OkHttpClient getClient(boolean acceptAllCertificates, @Nullable String username, @Nullable String password) {
        OkHttpClient.Builder builder = acceptAllCertificates ? getUnsafeOkHttpClientBuilder() : getDefaultOkHttpClientBuilder();

        if (username != null && password != null) {
            DigestAuthenticator authenticator = new DigestAuthenticator(new Credentials(username, password));
            builder = builder.authenticator(authenticator);
        }

        builder = builder.addNetworkInterceptor(new StethoInterceptor());
        return builder.build();
    }

    private static OkHttpClient.Builder getDefaultOkHttpClientBuilder() {
        return new OkHttpClient.Builder();
    }

    private static OkHttpClient.Builder getUnsafeOkHttpClientBuilder() {
        try {
            final X509TrustManager[] trustAllCerts = new X509TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, trustAllCerts[0])
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
