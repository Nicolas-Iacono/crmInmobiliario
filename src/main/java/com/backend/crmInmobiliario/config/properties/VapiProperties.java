package com.backend.crmInmobiliario.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "vapi")
public class VapiProperties {
    private String apiKey;
    private String baseUrl;
    private String assistantId;
    private String phoneNumberId;
}
