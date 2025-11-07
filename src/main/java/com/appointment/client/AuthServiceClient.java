package com.appointment.client;

import com.appointment.exception.NoDeviceTokenFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class AuthServiceClient {

    private final RestTemplate restTemplate;

    @Value("${service.auth-url}")
    private String authServiceUrl;

    @Value("${service.internal-secret}")
    private String internalServiceSecret;

    public AuthServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void updateUserStatus(String username, String status) {
        String url = "http://localhost:8080/api/auth" + username + "/status";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("status", status);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        restTemplate.exchange(url, HttpMethod.PATCH, entity, Void.class);
    }

    public String getDeviceToken(UUID userAuthId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Internal-Secret", internalServiceSecret);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String url = authServiceUrl + "/internal/auth/deviceToken/" + userAuthId;

            log.info("Calling auth service to fetch device token at URL: {}", url);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            log.info("Response received from the auth service {}", response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                String body = response.getBody();
                if (body == null || body.isBlank()) {
                    throw new NoDeviceTokenFoundException(userAuthId);
                }
                log.debug("Auth service returned token length: {}", body.length());
                return body;
            }

            if (response.getStatusCode().value() == 404) {
                throw new NoDeviceTokenFoundException(userAuthId);
            }

            throw new IllegalStateException("Auth service unexpected status: " + response.getStatusCode());

        } catch (HttpStatusCodeException e) {
            log.error("Auth service error ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("An exception occurred while connecting with the Auth service {}", e.getLocalizedMessage());
            return null;
        }
    }
}

