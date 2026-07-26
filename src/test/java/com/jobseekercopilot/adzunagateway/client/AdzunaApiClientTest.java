package com.jobseekercopilot.adzunagateway.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jobseekercopilot.adzunagateway.config.AdzunaProperties;
import com.jobseekercopilot.adzunagateway.model.dto.AdzunaSearchRequest;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
class AdzunaApiClientTest {

    private final AtomicInteger responseStatus = new AtomicInteger(200);
    private final AtomicReference<String> responseBody =
            new AtomicReference<>("""
                    {"count":0,"results":[]}
                    """);
    private final AtomicReference<URI> requestedUri = new AtomicReference<>();
    private HttpServer server;
    private AdzunaProperties properties;

    @BeforeEach
    void startProviderStub() throws IOException {
        server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", this::respond);
        server.start();

        properties = new AdzunaProperties();
        properties.setBaseUrl(
                "http://127.0.0.1:" + server.getAddress().getPort());
        properties.setAppId("synthetic-app");
        properties.setAppKey("synthetic-key");
        properties.setCountry("gb");
        properties.setResultsPerPage(25);
        properties.setPagesPerSearch(1);
        properties.setEnabled(true);
    }

    @AfterEach
    void stopProviderStub() {
        server.stop(0);
    }

    @Test
    void mapsRepresentativeProviderResponseAndRequestParameters() {
        responseBody.set("""
                {
                  "count": 12,
                  "results": [
                    {
                      "id": "adz-123",
                      "title": "Platform Engineer",
                      "description": "Build reliable services",
                      "company": {"display_name": "Example Ltd"},
                      "location": {
                        "display_name": "London",
                        "area": ["UK", "London"]
                      },
                      "latitude": 51.507351,
                      "longitude": -0.127758,
                      "salary_min": 65000,
                      "salary_max": 85000,
                      "salary_is_predicted": true,
                      "contract_type": "permanent",
                      "contract_time": "full_time",
                      "category": {"label": "IT Jobs"},
                      "created": "2026-07-24T09:00:00Z",
                      "redirect_url": "https://jobs.example.test/adz-123"
                    }
                  ]
                }
                """);

        var response = new AdzunaApiClient(properties)
                .search(request(2, 25));

        assertThat(response.getProvider()).isEqualTo("ADZUNA");
        assertThat(response.getPage()).isEqualTo(2);
        assertThat(response.getResultsPerPage()).isEqualTo(25);
        assertThat(response.getTotalAvailable()).isEqualTo(12);
        assertThat(response.getJobs()).singleElement().satisfies(job -> {
            assertThat(job.getExternalJobId()).isEqualTo("adz-123");
            assertThat(job.getTitle()).isEqualTo("Platform Engineer");
            assertThat(job.getCompanyName()).isEqualTo("Example Ltd");
            assertThat(job.getDescription())
                    .isEqualTo("Build reliable services");
            assertThat(job.getLocationDisplayName()).isEqualTo("London");
            assertThat(job.getLocationAreas())
                    .containsExactly("UK", "London");
            assertThat(job.getLatitude())
                    .isEqualByComparingTo("51.507351");
            assertThat(job.getLongitude())
                    .isEqualByComparingTo("-0.127758");
            assertThat(job.getSalaryMinimum()).isEqualTo(65000);
            assertThat(job.getSalaryMaximum()).isEqualTo(85000);
            assertThat(job.getSalaryPredicted()).isTrue();
            assertThat(job.getContractType()).isEqualTo("permanent");
            assertThat(job.getEmploymentType()).isEqualTo("full_time");
            assertThat(job.getCategory()).isEqualTo("IT Jobs");
            assertThat(job.getPostedAt())
                    .isEqualTo("2026-07-24T09:00:00Z");
            assertThat(job.getRedirectUrl())
                    .isEqualTo("https://jobs.example.test/adz-123");
        });
        assertThat(requestedUri.get().getPath())
                .isEqualTo("/jobs/gb/search/2");
        assertThat(requestedUri.get().getRawQuery())
                .contains("app_id=synthetic-app")
                .contains("app_key=synthetic-key")
                .contains("what=Platform%20Engineer")
                .contains("where=London")
                .contains("distance=15")
                .contains("results_per_page=25");
    }

    @Test
    void preservesMissingAndMalformedFieldsAsAbsent() {
        responseBody.set("""
                {
                  "count": 1,
                  "results": [
                    {
                      "id": "adz-sparse",
                      "location": {"area": []},
                      "latitude": "not-a-number",
                      "salary_min": "not-a-number",
                      "salary_is_predicted": "not-a-boolean",
                      "category": null
                    }
                  ]
                }
                """);

        var response = new AdzunaApiClient(properties)
                .search(request(null, null));

        assertThat(response.getJobs()).singleElement().satisfies(job -> {
            assertThat(job.getExternalJobId()).isEqualTo("adz-sparse");
            assertThat(job.getTitle()).isNull();
            assertThat(job.getCompanyName()).isNull();
            assertThat(job.getLocationDisplayName()).isNull();
            assertThat(job.getLocationAreas()).isEmpty();
            assertThat(job.getLatitude()).isNull();
            assertThat(job.getSalaryMinimum()).isNull();
            assertThat(job.getSalaryPredicted()).isNull();
            assertThat(job.getCategory()).isNull();
            assertThat(job.getRedirectUrl()).isNull();
        });
    }

    @Test
    void returnsContractValidEmptyResponseForZeroMatches() {
        var response = new AdzunaApiClient(properties)
                .search(request(3, 10));

        assertThat(response.getProvider()).isEqualTo("ADZUNA");
        assertThat(response.getPage()).isEqualTo(3);
        assertThat(response.getResultsPerPage()).isEqualTo(10);
        assertThat(response.getTotalAvailable()).isZero();
        assertThat(response.getJobs()).isEmpty();
    }

    @Test
    void translatesRateLimitAndUpstreamErrors() {
        responseStatus.set(429);
        assertThatThrownBy(() -> new AdzunaApiClient(properties)
                .search(request(null, null)))
                .isInstanceOf(
                        AdzunaApiClient.ProviderUnavailableException.class)
                .hasMessage("Adzuna rate limit exceeded");

        responseStatus.set(503);
        assertThatThrownBy(() -> new AdzunaApiClient(properties)
                .search(request(null, null)))
                .isInstanceOf(
                        AdzunaApiClient.ProviderUnavailableException.class)
                .hasMessage("Adzuna API request failed");
    }

    @Test
    void rejectsMissingLiveCredentialsInsteadOfReturningNoMatches() {
        properties.setAppId("");

        assertThatThrownBy(() -> new AdzunaApiClient(properties)
                        .search(request(null, null)))
                .isInstanceOf(
                        AdzunaApiClient.ProviderUnavailableException.class)
                .hasMessage(
                        "Adzuna live provider credentials are not configured");
        assertThat(requestedUri.get()).isNull();
    }

    @Test
    void explicitKillSwitchReturnsNoMatchesWithoutCredentials() {
        properties.setEnabled(false);
        properties.setAppId("");
        properties.setAppKey("");

        var response = new AdzunaApiClient(properties)
                .search(request(1, 10));

        assertThat(response.getTotalAvailable()).isZero();
        assertThat(response.getJobs()).isEmpty();
        assertThat(requestedUri.get()).isNull();
    }

    @Test
    void upstreamFailureLogsNeverContainQueryCredentials(
            CapturedOutput output) {
        String privateAppId = "private-app-id-for-redaction";
        String privateAppKey = "private-app-key-for-redaction";
        properties.setAppId(privateAppId);
        properties.setAppKey(privateAppKey);
        responseStatus.set(503);

        assertThatThrownBy(() -> new AdzunaApiClient(properties)
                        .search(request(null, null)))
                .isInstanceOf(
                        AdzunaApiClient.ProviderUnavailableException.class)
                .hasMessage("Adzuna API request failed");

        assertThat(output)
                .contains("status=503")
                .doesNotContain(
                        privateAppId,
                        privateAppKey,
                        "app_id=",
                        "app_key=");
    }

    private AdzunaSearchRequest request(
            Integer page,
            Integer resultsPerPage) {
        AdzunaSearchRequest request = new AdzunaSearchRequest();
        request.setTargetRole("Platform Engineer");
        request.setLocation("London");
        request.setDistanceMiles(15);
        request.setPage(page);
        request.setResultsPerPage(resultsPerPage);
        return request;
    }

    private void respond(HttpExchange exchange) throws IOException {
        requestedUri.set(exchange.getRequestURI());
        byte[] body = responseBody.get().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set(
                "Content-Type", "application/json");
        exchange.sendResponseHeaders(responseStatus.get(), body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }
}
