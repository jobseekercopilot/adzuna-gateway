package com.jobseekercopilot.adzunagateway.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jobseekercopilot.adzunagateway.client.AdzunaProviderClient;
import com.jobseekercopilot.adzunagateway.model.dto.AdzunaJob;
import com.jobseekercopilot.adzunagateway.model.dto.AdzunaSearchRequest;
import com.jobseekercopilot.adzunagateway.model.dto.AdzunaSearchResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AdzunaSearchControllerTest {

    @Test
    void exposesStableGatewaySearchContract() throws Exception {
        AdzunaProviderClient provider = mock(AdzunaProviderClient.class);
        AdzunaJob job = new AdzunaJob();
        job.setExternalJobId("adz-1");
        job.setTitle("Engineer");
        AdzunaSearchResponse response = new AdzunaSearchResponse();
        response.setPage(1);
        response.setResultsPerPage(10);
        response.setTotalAvailable(1);
        response.setJobs(List.of(job));
        when(provider.search(any())).thenReturn(response);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(
                new AdzunaSearchController(provider)).build();

        mvc.perform(post("/api/v1/adzuna/jobs/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetRole": "Engineer",
                                  "location": "London",
                                  "distanceMiles": 20,
                                  "page": 1,
                                  "resultsPerPage": 10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("ADZUNA"))
                .andExpect(jsonPath("$.totalAvailable").value(1))
                .andExpect(jsonPath("$.jobs[0].externalJobId")
                        .value("adz-1"))
                .andExpect(jsonPath("$.jobs[0].title")
                        .value("Engineer"));

        ArgumentCaptor<AdzunaSearchRequest> request =
                ArgumentCaptor.forClass(AdzunaSearchRequest.class);
        verify(provider).search(request.capture());
        org.assertj.core.api.Assertions.assertThat(
                request.getValue().getTargetRole()).isEqualTo("Engineer");
        org.assertj.core.api.Assertions.assertThat(
                request.getValue().getDistanceMiles()).isEqualTo(20);
    }
}
