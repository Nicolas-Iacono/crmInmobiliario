package com.backend.crmInmobiliario.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean @Qualifier("googleApis")
    public WebClient googleApis(WebClient.Builder b) {
        return b.baseUrl("https://www.googleapis.com").build();
    }

    @Bean @Qualifier("googleOAuth")
    public WebClient googleOAuth(WebClient.Builder b) {
        return b.baseUrl("https://oauth2.googleapis.com").build();
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://primary-production-9170b.up.railway.app/webhook")
                .build();
    }
}

