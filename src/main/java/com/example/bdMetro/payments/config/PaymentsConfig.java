package com.example.bdMetro.payments.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({DlocalProperties.class, MembershipCatalogProperties.class, FxRateProperties.class})
public class PaymentsConfig {

    @Bean
    RestClient dlocalRestClient(DlocalProperties properties) {
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
