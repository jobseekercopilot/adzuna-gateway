package com.jobseekercopilot.adzunagateway.client;

import com.jobseekercopilot.adzunagateway.config.FixtureProperties;
import com.jobseekercopilot.adzunagateway.model.dto.AdzunaJob;
import com.jobseekercopilot.adzunagateway.model.dto.AdzunaSearchRequest;
import com.jobseekercopilot.adzunagateway.model.dto.AdzunaSearchResponse;
import com.jobseekercopilot.generated.systemdataservice.api.FixtureControllerApi;
import com.jobseekercopilot.generated.systemdataservice.model.DemoJob;
import com.jobseekercopilot.generated.systemdataservice.model.FixtureJobSearchResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "external-provider", name = "mode", havingValue = "FIXTURE")
public class FixtureAdzunaProviderClient implements AdzunaProviderClient {
    private static final Logger log = LoggerFactory.getLogger(FixtureAdzunaProviderClient.class);
    private final FixtureProperties fixtureProperties;
    private final FixtureControllerApi fixtureControllerApi;

    public FixtureAdzunaProviderClient(FixtureProperties fixtureProperties, FixtureControllerApi fixtureControllerApi) {
        this.fixtureProperties = fixtureProperties;
        this.fixtureControllerApi = fixtureControllerApi;
    }

    @Override
    public AdzunaSearchResponse search(AdzunaSearchRequest request) {
        delay();
        int page = request.getPage() == null ? 1 : request.getPage();
        int pageSize = request.getResultsPerPage() == null ? 10 : request.getResultsPerPage();
        FixtureJobSearchResponse body = fixtureControllerApi.searchJobs(
                fixtureProperties.getDatasetId(),
                fixtureProperties.getDatasetVersion(),
                fixtureProperties.getScenario(),
                "ADZUNA",
                request.getTargetRole(),
                request.getLocation(),
                Math.max(0, page - 1),
                pageSize,
                null,
                null,
                null,
                null);
        AdzunaSearchResponse response = new AdzunaSearchResponse();
        response.setPage(page);
        response.setResultsPerPage(pageSize);
        response.setTotalAvailable(number(body == null ? null : body.getTotalResults()));
        response.setJobs(body == null || body.getJobs() == null ? List.of() : body.getJobs().stream().map(this::toJob).toList());
        log.info("Adzuna fixture search returned resultCount={} totalAvailable={} datasetId={} scenario={}",
                response.getJobs().size(), response.getTotalAvailable(), fixtureProperties.getDatasetId(), fixtureProperties.getScenario());
        return response;
    }

    private AdzunaJob toJob(DemoJob source) {
        AdzunaJob job = new AdzunaJob();
        job.setExternalJobId(text(source.getExternalReference(), source.getId()));
        job.setTitle(source.getTitle());
        job.setCompanyName(text(source.getCompanyDisplayName(), source.getCompanyName()));
        job.setDescription(source.getDescription());
        job.setLocationDisplayName(source.getLocationName());
        job.setLocationAreas(new ArrayList<>(List.of("UK", text(source.getRegion(), ""), text(source.getLocationName(), ""))).stream()
                .filter(value -> value != null && !value.isBlank()).toList());
        job.setLatitude(decimal(source.getLatitude()));
        job.setLongitude(decimal(source.getLongitude()));
        job.setSalaryMinimum(source.getSalaryMinimum());
        job.setSalaryMaximum(source.getSalaryMaximum());
        job.setSalaryPredicted(false);
        job.setContractType(source.getContractType());
        job.setEmploymentType(source.getEmploymentType());
        job.setRemoteType(source.getRemoteType());
        job.setCategory(source.getCategory());
        job.setPostedAt(source.getDatePosted());
        job.setRedirectUrl(text(source.getSourceUrl(), "https://fixtures.jobseekercopilot.local/adzuna/" + job.getExternalJobId()));
        return job;
    }

    private void delay() {
        if (!fixtureProperties.getLatency().isEnabled() || fixtureProperties.getLatency().getJobSearchMs() <= 0) {
            return;
        }
        try {
            Thread.sleep(fixtureProperties.getLatency().getJobSearchMs());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private String text(Object value, String fallback) {
        return value == null ? fallback : value.toString();
    }

    private int number(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal decimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }
}
