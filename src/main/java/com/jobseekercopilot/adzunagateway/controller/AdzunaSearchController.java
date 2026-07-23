package com.jobseekercopilot.adzunagateway.controller;

import com.jobseekercopilot.adzunagateway.client.AdzunaProviderClient;
import com.jobseekercopilot.adzunagateway.model.dto.AdzunaSearchRequest;
import com.jobseekercopilot.adzunagateway.model.dto.AdzunaSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/adzuna/jobs")
@Tag(name = "Adzuna Jobs")
public class AdzunaSearchController {
    private final AdzunaProviderClient adzunaApiClient;

    public AdzunaSearchController(AdzunaProviderClient adzunaApiClient) {
        this.adzunaApiClient = adzunaApiClient;
    }

    @PostMapping("/search")
    @Operation(summary = "Search Adzuna jobs for job-service")
    public ResponseEntity<AdzunaSearchResponse> search(@RequestBody AdzunaSearchRequest request) {
        return ResponseEntity.ok(adzunaApiClient.search(request));
    }
}
