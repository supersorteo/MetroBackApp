package com.example.bdMetro.payments.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({MercadoPagoProperties.class, MembershipCatalogProperties.class, FxRateProperties.class, PayPalProperties.class})
public class PaymentsConfig {

    private HttpComponentsClientHttpRequestFactory apacheFactory() {
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(Timeout.ofMilliseconds(3000))
            .setResponseTimeout(Timeout.ofMilliseconds(10000))
            .build();
        CloseableHttpClient httpClient = HttpClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .build();
        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

    @Bean
    RestClient mercadoPagoRestClient(MercadoPagoProperties properties) {
        return RestClient.builder()
                .requestFactory(apacheFactory())
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    @Bean
    RestClient fxRestClient(FxRateProperties properties) {
        return RestClient.builder()
                .requestFactory(apacheFactory())
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    @Bean
    RestClient payPalRestClient(PayPalProperties properties) {
        return RestClient.builder()
                .requestFactory(apacheFactory())
                .baseUrl(properties.getBaseUrl())
                .build();
    }
}
