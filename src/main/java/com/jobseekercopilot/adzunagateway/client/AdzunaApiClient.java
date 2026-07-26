package com.jobseekercopilot.adzunagateway.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.jobseekercopilot.adzunagateway.config.AdzunaProperties;
import com.jobseekercopilot.adzunagateway.model.dto.AdzunaJob;
import com.jobseekercopilot.adzunagateway.model.dto.AdzunaSearchRequest;
import com.jobseekercopilot.adzunagateway.model.dto.AdzunaSearchResponse;
import com.jobseekercopilot.adzunagateway.logging.CorrelationIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "external-provider", name = "mode", havingValue = "LIVE", matchIfMissing = true)
public class AdzunaApiClient implements AdzunaProviderClient {
    private static final Logger log = LoggerFactory.getLogger(AdzunaApiClient.class);

    private final AdzunaProperties properties;
    private final WebClient webClient;

    public AdzunaApiClient(AdzunaProperties properties) {
        this.properties = properties;
        this.webClient = WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .filter((request, next) -> {
                    String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
                    if (StringUtils.hasText(correlationId)) {
                        return next.exchange(org.springframework.web.reactive.function.client.ClientRequest.from(request)
                                .header(CorrelationIdFilter.HEADER_NAME, correlationId)
                                .build());
                    }
                    return next.exchange(request);
                })
                .build();
    }

    @Override
    public AdzunaSearchResponse search(AdzunaSearchRequest request) {
        if (!properties.isEnabled()) {
            log.warn("Adzuna provider is disabled");
            return empty(request);
        }
        if (blank(properties.getAppId()) || blank(properties.getAppKey())) {
            throw new ProviderUnavailableException(
                    "Adzuna live provider credentials are not configured");
        }
        long startedAt = System.nanoTime();
        log.info("Adzuna provider request started targetRole={} location={} page={} resultsPerPage={}",
                request.getTargetRole(),
                request.getLocation(),
                request.getPage(),
                request.getResultsPerPage());
        try {
            AdzunaSearchResponse combined = empty(request);
            int requestedPage = request.getPage() == null ? 1 : request.getPage();
            int resultsPerPage = request.getResultsPerPage() == null ? properties.getResultsPerPage() : request.getResultsPerPage();
            combined.setResultsPerPage(resultsPerPage);
            for (int offset = 0; offset < Math.max(1, properties.getPagesPerSearch()); offset++) {
                int page = requestedPage + offset;
                JsonNode body = webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/jobs/{country}/search/{page}")
                                .queryParam("app_id", properties.getAppId())
                                .queryParam("app_key", properties.getAppKey())
                                .queryParam("what", request.getTargetRole())
                                .queryParam("where", request.getLocation())
                                .queryParam("distance", request.getDistanceMiles())
                                .queryParam("results_per_page", resultsPerPage)
                                .build(properties.getCountry(), page))
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .block();
                if (body == null) {
                    continue;
                }
                combined.setTotalAvailable(body.path("count").isNumber() ? body.path("count").asInt() : combined.getTotalAvailable());
                body.path("results").forEach(node -> combined.getJobs().add(toJob(node)));
            }
            log.info("Adzuna provider returned status=200 resultCount={} totalAvailable={} durationMs={}",
                    combined.getJobs().size(),
                    combined.getTotalAvailable(),
                    (System.nanoTime() - startedAt) / 1_000_000);
            return combined;
        } catch (WebClientResponseException.TooManyRequests ex) {
            log.warn("Adzuna provider rate limited status={} durationMs={}",
                    ex.getStatusCode().value(),
                    (System.nanoTime() - startedAt) / 1_000_000);
            throw new ProviderUnavailableException("Adzuna rate limit exceeded", ex);
        } catch (WebClientResponseException ex) {
            log.warn("Adzuna provider failed status={} durationMs={} error={}",
                    ex.getStatusCode().value(),
                    (System.nanoTime() - startedAt) / 1_000_000,
                    ex.getClass().getSimpleName());
            throw new ProviderUnavailableException("Adzuna API request failed", ex);
        } catch (RuntimeException ex) {
            log.warn("Adzuna provider failed durationMs={} error={}",
                    (System.nanoTime() - startedAt) / 1_000_000,
                    ex.getClass().getSimpleName());
            throw new ProviderUnavailableException("Adzuna API request failed", ex);
        }
    }

    private AdzunaSearchResponse empty(AdzunaSearchRequest request) {
        AdzunaSearchResponse response = new AdzunaSearchResponse();
        response.setPage(request.getPage() == null ? 1 : request.getPage());
        response.setResultsPerPage(request.getResultsPerPage() == null ? properties.getResultsPerPage() : request.getResultsPerPage());
        response.setTotalAvailable(0);
        response.setJobs(new ArrayList<>());
        return response;
    }

    private AdzunaJob toJob(JsonNode node) {
        AdzunaJob job = new AdzunaJob();
        job.setExternalJobId(text(node, "id"));
        job.setTitle(text(node, "title"));
        job.setDescription(text(node, "description"));
        job.setCompanyName(text(node.path("company"), "display_name"));
        job.setLocationDisplayName(text(node.path("location"), "display_name"));
        List<String> areas = new ArrayList<>();
        node.path("location").path("area").forEach(area -> areas.add(area.asText()));
        job.setLocationAreas(areas);
        job.setLatitude(decimal(node, "latitude"));
        job.setLongitude(decimal(node, "longitude"));
        job.setSalaryMinimum(integer(node, "salary_min"));
        job.setSalaryMaximum(integer(node, "salary_max"));
        job.setSalaryPredicted(booleanValue(node, "salary_is_predicted"));
        job.setContractType(text(node, "contract_type"));
        job.setEmploymentType(text(node, "contract_time"));
        job.setCategory(text(node.path("category"), "label"));
        job.setPostedAt(text(node, "created"));
        job.setRedirectUrl(text(node, "redirect_url"));
        return job;
    }

    private String text(JsonNode node, String field) {
        return node == null || node.path(field).isMissingNode() || node.path(field).isNull() ? null : node.path(field).asText();
    }

    private Integer integer(JsonNode node, String field) {
        return node.path(field).isNumber() ? node.path(field).asInt() : null;
    }

    private BigDecimal decimal(JsonNode node, String field) {
        return node.path(field).isNumber() ? node.path(field).decimalValue() : null;
    }

    private Boolean booleanValue(JsonNode node, String field) {
        return node.path(field).isBoolean()
                ? node.path(field).booleanValue()
                : null;
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public static class ProviderUnavailableException extends RuntimeException {
        public ProviderUnavailableException(String message) {
            super(message);
        }

        public ProviderUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
