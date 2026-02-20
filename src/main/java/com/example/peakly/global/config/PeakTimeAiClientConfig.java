package com.example.peakly.global.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.concurrent.TimeUnit;

@Configuration
public class PeakTimeAiClientConfig {

    @Value("${peaktime.ai.base-url}")
    private String aiBaseUrl;

    @Bean(name = "peakTimeAiHttpClient")
    public RestClient peakTimeAiRestClient() {

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(3, TimeUnit.SECONDS)
                .setResponseTimeout(7, TimeUnit.SECONDS)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .evictIdleConnections(TimeValue.ofSeconds(30))
                .build();

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        return RestClient.builder()
                .baseUrl(aiBaseUrl)
                .requestFactory(factory)
                .build();
    }
}