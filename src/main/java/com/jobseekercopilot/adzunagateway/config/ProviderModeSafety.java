package com.jobseekercopilot.adzunagateway.config;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ProviderModeSafety implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(ProviderModeSafety.class);
    private final ExternalProviderProperties providerProperties;
    private final AdzunaProperties adzunaProperties;
    private final FixtureProperties fixtureProperties;
    private final Environment environment;

    public ProviderModeSafety(
            ExternalProviderProperties providerProperties,
            AdzunaProperties adzunaProperties,
            FixtureProperties fixtureProperties,
            Environment environment) {
        this.providerProperties = providerProperties;
        this.adzunaProperties = adzunaProperties;
        this.fixtureProperties = fixtureProperties;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean production = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("prod") || profile.equalsIgnoreCase("production"));
        if (production && providerProperties.getMode() == ExternalProviderMode.FIXTURE) {
            throw new IllegalStateException("adzuna-gateway cannot start in FIXTURE mode with a production profile.");
        }
        boolean credentialsConfigured = !blank(adzunaProperties.getAppId())
                && !blank(adzunaProperties.getAppKey());
        boolean liveEnabled = providerProperties.getMode() == ExternalProviderMode.LIVE
                && adzunaProperties.isEnabled();
        if (liveEnabled && !credentialsConfigured) {
            throw new IllegalStateException(
                    "Adzuna LIVE mode requires both ADZUNA_APP_ID and ADZUNA_APP_KEY.");
        }
        log.info(
                "provider mode active gateway=adzuna-gateway mode={} datasetId={} datasetVersion={} scenario={} externalCallsEnabled={} credentialsConfigured={}",
                providerProperties.getMode(),
                fixtureProperties.getDatasetId(),
                fixtureProperties.getDatasetVersion(),
                fixtureProperties.getScenario(),
                liveEnabled,
                credentialsConfigured);
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
