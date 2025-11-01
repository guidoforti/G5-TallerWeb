package com.tallerwebi.config;

import com.mercadopago.MercadoPagoConfig;
import org.springframework.context.annotation.Configuration;
import javax.annotation.PostConstruct;

@Configuration
public class MercadoPagoConfigurer {
    
    @PostConstruct
    public void configMercadoPago() {
        // Obtener el token de acceso de MercadoPago desde las variables de entorno
        //String accessToken = System.getenv("TEST_ACCESS_TOKEN");

        // Configurar el token de acceso de MercadoPago
        MercadoPagoConfig.setAccessToken("APP_USR-5055237229425559-110110-7c93233628fe27743f5f5960bab8da12-2959447417");
    }
}
