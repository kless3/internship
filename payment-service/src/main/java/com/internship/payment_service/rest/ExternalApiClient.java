package com.internship.payment_service.rest;

import com.internship.payment_service.rest.property.ExternalApiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class ExternalApiClient {

    private static final int LOCAL_RANDOM_MIN = 1;
    private static final int LOCAL_RANDOM_MAX = 100;
    private static final String FAILED_TO_GET_RANDOM_NUMBER = "Failed to get random number. Status: ";
    private static final String EMPTY_RESPONSE_BODY = "Empty response body from external API";
    private static final String INVALID_NUMBER_FORMAT = "Invalid number format in response: ";
    private static final String NULL_RESPONSE = "Response is null";
    private static final String NULL_RESPONSE_BODY = "Response body is null";

    private final RestTemplate restTemplate;
    private final ExternalApiProperties externalApiProperties;

    @Retryable(retryFor = {HttpServerErrorException.class, ResourceAccessException.class}, maxAttempts = 3)
    public int getRandomNumber() {
        String url = externalApiProperties.getUrl();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return parseResponse(response);
        } catch (Exception e) {
            return generateRandomNumberLocal();
        }
    }

    private int parseResponse(ResponseEntity<String> response) {
        if (response == null) {
            throw new RuntimeException(NULL_RESPONSE);
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException(FAILED_TO_GET_RANDOM_NUMBER + response.getStatusCode());
        }

        String responseBody = response.getBody();
        if (responseBody == null) {
            throw new RuntimeException(NULL_RESPONSE_BODY);
        }

        String trimmedBody = responseBody.trim();
        if (trimmedBody.isEmpty()) {
            throw new RuntimeException(EMPTY_RESPONSE_BODY);
        }

        try {
            return Integer.parseInt(trimmedBody);
        } catch (NumberFormatException e) {
            throw new RuntimeException(INVALID_NUMBER_FORMAT + responseBody, e);
        }
    }

    private int generateRandomNumberLocal() {
        return ThreadLocalRandom.current().nextInt(LOCAL_RANDOM_MIN, LOCAL_RANDOM_MAX + 1);
    }
}