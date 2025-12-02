package com.appointment.client;

import com.appointment.dto.*;
import com.appointment.exception.ApiException;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
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

        log.info("Calling appointment availability URL {}", url);

        ResponseEntity<Boolean> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, Boolean.class
        );

        log.info("Response received from profile service for appointment availability {}", response.getBody());

        return response.getBody() != null && response.getBody();
    }

    public List<String> getAvailableLawyers(OpenAppointmentSearchDTO openAppointmentSearchDTO) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Secret", internalServiceSecret);

        log.info("Sending request to profile service: {}", openAppointmentSearchDTO);

        String url = profileServiceUrl + "/internal/searchLawyersBySpecialityAndAvailability";

        log.info("Calling searchLawyersBySpecialityAndAvailability URL: {}", url);

        HttpEntity<OpenAppointmentSearchDTO> entity = new HttpEntity<>(openAppointmentSearchDTO, headers);

        ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                List.class
        );

        log.info("Response received from profile service for searchLawyersBySpecialityAndAvailability: {}", response);

        return response.getBody();
    }

    public boolean isCustomerExist(UUID customerAuthId) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Secret", internalServiceSecret);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        String url = profileServiceUrl + "/internal/customer/" + customerAuthId + "/exists";

        log.info("Calling customer existence URL from profile service: {}", url);

        try {
            ResponseEntity<Boolean> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity, Boolean.class
            );

            log.info("Received response from profile service: {}", response);

            return Boolean.TRUE.equals(response.getBody());
        } catch (HttpClientErrorException e) {
            return false;
        }
    }

    public CustomerDTO fetchCustomerIfExists(UUID customerId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Secret", internalServiceSecret);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        String url = profileServiceUrl + "/internal/customer/" + customerId + "/details";

        try {
            ResponseEntity<CustomerDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    CustomerDTO.class
            );

            log.info("Received response from profile service: {}", response.getBody());

            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    public LawyerDetailsDTO fetchLawyerServices(UUID lawyerId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Secret", internalServiceSecret);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        String url = profileServiceUrl + "/internal/lawyer/" + lawyerId;

        log.info("Calling the profile service to fetch the lawyer services: {}", url);

        try {
            LawyerDetailsDTO response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    LawyerDetailsDTO.class
            ).getBody();

            log.info("Received response from profile service: {}", response);

            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Error while calling profile service (Status: {}): {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Profile service unreachable: {}", e.getMessage(), e);
            throw new ApiException("Profile service unreachable", "PROFILE_SERVICE_DOWN", HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            log.error("Unexpected error while fetching lawyer services: {}", e.getMessage(), e);
            throw new ApiException("Unexpected error", "UNEXPECTED_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public UUID fetchLawyerId(UUID lawyerAuthUserId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Secret", internalServiceSecret);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        String url = profileServiceUrl + "/internal/lawyer/" + lawyerAuthUserId + "/uuid";

        log.info("Calling the profile service to fetch the lawyer id: {}", url);

        try {
            UUID response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    UUID.class
            ).getBody();

            log.info("Received response from profile service: {}", response);

            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Error while calling profile service (Status: {}): {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Profile service unreachable: {}", e.getMessage(), e);
            throw new ApiException("Profile service unreachable", "PROFILE_SERVICE_DOWN", HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            log.error("Unexpected error while fetching lawyer services: {}", e.getMessage(), e);
            throw new ApiException("Unexpected error", "UNEXPECTED_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
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

    public boolean isLawyerValidByLawyerId(UUID lawyerId) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Secret", internalServiceSecret);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        String url = profileServiceUrl + "/internal/lawyer/" + lawyerId + "/exists";

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
