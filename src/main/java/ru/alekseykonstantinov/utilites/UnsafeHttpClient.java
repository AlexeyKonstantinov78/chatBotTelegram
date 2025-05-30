package ru.alekseykonstantinov.utilites;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public class UnsafeHttpClient {
    public static CloseableHttpClient create() {
        try {
            // Создаем стратегию доверия, которая принимает все сертификаты
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

            // Создаем SSL контекст с нашей стратегией доверия
            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();

            // Создаем фабрику сокетов с нашим SSL контекстом и без проверки имени хоста
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                    sslContext,
                    NoopHostnameVerifier.INSTANCE);

            // Создаем HttpClient с нашей фабрикой сокетов
            return HttpClients.custom()
                    .setSSLSocketFactory(sslSocketFactory)
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new RuntimeException("Failed to create unsafe HttpClient", e);
        }
    }
}
