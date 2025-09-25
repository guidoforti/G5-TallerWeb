package com.tallerwebi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {
    @Bean
    public ManualModelMapper manualModelMapper() {
        return new ManualModelMapper();
    }
}
