package com.appointment.client;

import com.appointment.dto.AvailabilityCheckRequestDTO;
import com.appointment.dto.OpenAppointmentSearchDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Service
public class ProfileServiceClient {

    private final RestTemplate restTemplate;

    @Value("${service.profile-url}")
    private String profileServiceUrl;

    @Value("${service.internal-secret}")
    private String internalServiceSecret;

    public ProfileServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean checkAppointmentAvailability(AvailabilityCheckRequestDTO availabilityCheckRequestDTO) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Secret", internalServiceSecret);

        HttpEntity<AvailabilityCheckRequestDTO> entity = new HttpEntity<>(availabilityCheckRequestDTO, headers);

        String url = profileServiceUrl + "/internal/appointmentAvailability";

        ResponseEntity<Boolean> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, Boolean.class
        );

        return response.getBody() != null && response.getBody();
    }

    public List<String> getAvailableLawyers(OpenAppointmentSearchDTO openAppointmentSearchDTO) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Secret", internalServiceSecret);

        String url = profileServiceUrl + "/internal/searchLawyersBySpecialityAndAvailability";

        HttpEntity<OpenAppointmentSearchDTO> entity = new HttpEntity<>(openAppointmentSearchDTO, headers);

        ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                List.class
        );

        return response.getBody();
    }

    public boolean isCustomerExist(UUID customerId) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Secret", internalServiceSecret);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        String url = profileServiceUrl + "/internal/customer/" + customerId + "/exists";

        try {
            ResponseEntity<Boolean> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity, Boolean.class
            );
            return Boolean.TRUE.equals(response.getBody());
        } catch (HttpClientErrorException e) {
            return false;
        }
    }

    public boolean isLawyerValid(UUID authUserId) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Secret", internalServiceSecret);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        String url = profileServiceUrl + "/internal/lawyer/" + authUserId + "/validate";

        try {
            ResponseEntity<Boolean> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Boolean.class
            );
            return Boolean.TRUE.equals(response.getBody());
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            return false;
        }
    }


}
