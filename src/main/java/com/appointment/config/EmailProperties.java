package com.appointment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "email")
public class EmailProperties {
    private Smtp smtp;
    private From from;
    private String to;
    private String subject;
    private String body;

    @Data
    public static class Smtp {
        private String host;
        private int port;
        private String protocols;
        private boolean auth;
        private boolean starttls;
        private String trust;
        private String protocol;
    }

    @Data
    public static class From {
        private String address;
        private String password;
        private String name;
    }
}

