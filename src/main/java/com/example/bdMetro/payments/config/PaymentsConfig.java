package com.example.bdMetro.payments.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({MercadoPagoProperties.class, MembershipCatalogProperties.class, FxRateProperties.class})
public class PaymentsConfig {

    @Bean
    RestClient mercadoPagoRestClient(MercadoPagoProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    @Bean
    RestClient fxRestClient(FxRateProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }
}
