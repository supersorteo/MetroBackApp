package com.example.bdMetro.payments.config;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({MercadoPagoProperties.class, MembershipCatalogProperties.class, FxRateProperties.class, PayPalProperties.class})
public class PaymentsConfig {

    private HttpComponentsClientHttpRequestFactory apacheFactory() {
        return new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault());
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
