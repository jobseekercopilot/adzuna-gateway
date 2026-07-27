package com.jobseekercopilot.adzunagateway.controller;

import com.jobseekercopilot.adzunagateway.client.AdzunaApiClient;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProviderExceptionHandler {

    @ExceptionHandler(AdzunaApiClient.ProviderUnavailableException.class)
    ResponseEntity<Map<String, String>> handleProviderFailure(
            AdzunaApiClient.ProviderUnavailableException exception) {
        return ResponseEntity.status(exception.getStatus()).body(Map.of(
                "code", safeCode(exception),
                "message", safeMessage(exception)));
    }

    private String safeCode(
            AdzunaApiClient.ProviderUnavailableException exception) {
        return switch (exception.getStatus()) {
            case TOO_MANY_REQUESTS -> "RATE_LIMITED";
            case UNAUTHORIZED, FORBIDDEN -> "CONFIGURATION_ERROR";
            default -> "PROVIDER_UNAVAILABLE";
        };
    }

    private String safeMessage(
            AdzunaApiClient.ProviderUnavailableException exception) {
        return switch (exception.getStatus()) {
            case TOO_MANY_REQUESTS -> "Adzuna rate limit reached";
            case UNAUTHORIZED, FORBIDDEN -> "Adzuna configuration rejected";
            default -> "Adzuna is temporarily unavailable";
        };
    }
}
