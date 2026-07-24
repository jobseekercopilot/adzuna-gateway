package com.jobseekercopilot.adzunagateway.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jobseekercopilot.adzunagateway.config.FixtureProperties;
import com.jobseekercopilot.adzunagateway.model.dto.AdzunaSearchRequest;
import com.jobseekercopilot.generated.systemdataservice.api.FixtureControllerApi;
import com.jobseekercopilot.generated.systemdataservice.model.DemoJob;
import com.jobseekercopilot.generated.systemdataservice.model.FixtureJobSearchResponse;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;

class FixtureAdzunaProviderClientTest {

    private final FixtureProperties properties = properties();
    private final FixtureControllerApi fixtureApi =
            mock(FixtureControllerApi.class);
    private final FixtureAdzunaProviderClient client =
            new FixtureAdzunaProviderClient(properties, fixtureApi);

    @Test
    void mapsPopulatedSystemDataFixtureAndRequest() {
        DemoJob source = new DemoJob()
                .id("fixture-id")
                .externalReference("adz-fixture-1")
                .title("Fixture Engineer")
                .companyName("Fallback Company")
                .companyDisplayName("Fixture Company")
                .description("Synthetic fixture")
                .locationName("Leeds")
                .region("Yorkshire")
                .latitude(53.8008)
                .longitude(-1.5491)
                .salaryMinimum(50000)
                .salaryMaximum(70000)
                .contractType("PERMANENT")
                .employmentType("FULL_TIME")
                .category("Technology")
                .datePosted("2026-07-20T10:00:00Z")
                .sourceUrl(URI.create(
                        "https://fixtures.example.test/adz-fixture-1"));
        FixtureJobSearchResponse fixture = new FixtureJobSearchResponse()
                .page(1)
                .pageSize(5)
                .totalResults(7)
                .jobs(List.of(source));
        when(fixtureApi.searchJobs(
                "dataset", "2.0", "DEMO_READY", "ADZUNA",
                "Engineer", "Leeds", 1, 5,
                null, null, null, null))
                .thenReturn(fixture);

        AdzunaSearchRequest request = new AdzunaSearchRequest();
        request.setTargetRole("Engineer");
        request.setLocation("Leeds");
        request.setPage(2);
        request.setResultsPerPage(5);

        var response = client.search(request);

        verify(fixtureApi).searchJobs(
                "dataset", "2.0", "DEMO_READY", "ADZUNA",
                "Engineer", "Leeds", 1, 5,
                null, null, null, null);
        assertThat(response.getPage()).isEqualTo(2);
        assertThat(response.getResultsPerPage()).isEqualTo(5);
        assertThat(response.getTotalAvailable()).isEqualTo(7);
        assertThat(response.getJobs()).singleElement().satisfies(job -> {
            assertThat(job.getExternalJobId())
                    .isEqualTo("adz-fixture-1");
            assertThat(job.getTitle()).isEqualTo("Fixture Engineer");
            assertThat(job.getCompanyName()).isEqualTo("Fixture Company");
            assertThat(job.getLocationDisplayName()).isEqualTo("Leeds");
            assertThat(job.getLocationAreas())
                    .containsExactly("UK", "Yorkshire", "Leeds");
            assertThat(job.getLatitude()).isEqualByComparingTo("53.8008");
            assertThat(job.getSalaryMinimum()).isEqualTo(50000);
            assertThat(job.getSalaryPredicted()).isFalse();
            assertThat(job.getRedirectUrl()).isEqualTo(
                    "https://fixtures.example.test/adz-fixture-1");
        });
    }

    @Test
    void returnsDeterministicEmptyResultForNullOrEmptyFixtureBody() {
        AdzunaSearchRequest request = new AdzunaSearchRequest();
        request.setTargetRole("No Matches");
        when(fixtureApi.searchJobs(
                "dataset", "2.0", "DEMO_READY", "ADZUNA",
                "No Matches", null, 0, 10,
                null, null, null, null))
                .thenReturn(null);

        var response = client.search(request);

        assertThat(response.getProvider()).isEqualTo("ADZUNA");
        assertThat(response.getTotalAvailable()).isZero();
        assertThat(response.getJobs()).isEmpty();
    }

    private FixtureProperties properties() {
        FixtureProperties result = new FixtureProperties();
        result.setDatasetId("dataset");
        result.setDatasetVersion("2.0");
        result.setScenario("DEMO_READY");
        return result;
    }
}
