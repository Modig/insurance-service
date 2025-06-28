package dev.modig.insurance.config;

import org.ff4j.FF4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeatureToggleConfig {

    @Bean
    public FF4j ff4j() {
        FF4j ff4j = new FF4j();
        ff4j.createFeature("DISCOUNT_CAMPAIGN");
        ff4j.getFeatureStore().enable("DISCOUNT_CAMPAIGN");
        return ff4j;
    }
}

