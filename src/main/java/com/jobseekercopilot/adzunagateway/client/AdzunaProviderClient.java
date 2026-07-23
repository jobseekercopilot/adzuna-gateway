package com.jobseekercopilot.adzunagateway.client;

import com.jobseekercopilot.adzunagateway.model.dto.AdzunaSearchRequest;
import com.jobseekercopilot.adzunagateway.model.dto.AdzunaSearchResponse;

public interface AdzunaProviderClient {
    AdzunaSearchResponse search(AdzunaSearchRequest request);
}
