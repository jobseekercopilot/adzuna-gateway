package com.jobseekercopilot.adzunagateway.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AdzunaPropertiesTest {

    @Test
    void defaultsMatchTheCurrentRealProviderSettings() {
        AdzunaProperties properties = new AdzunaProperties();

        assertThat(properties.getResultsPerPage())
                .isEqualTo(AdzunaProperties.DEFAULT_RESULTS_PER_PAGE);
        assertThat(properties.getPagesPerSearch())
                .isEqualTo(AdzunaProperties.DEFAULT_PAGES_PER_SEARCH);
    }

    @ParameterizedTest
    @CsvSource({
        "-100, 1",
        "0, 1",
        "1, 1",
        "50, 50",
        "51, 50",
        "2147483647, 50"
    })
    void clampsConfiguredResultsPerPage(
            int configured,
            int expected) {
        AdzunaProperties properties = new AdzunaProperties();

        properties.setResultsPerPage(configured);

        assertThat(properties.getResultsPerPage()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "-100, 1",
        "0, 1",
        "1, 1",
        "2, 2",
        "3, 2",
        "2147483647, 2"
    })
    void clampsConfiguredProviderPageBudget(
            int configured,
            int expected) {
        AdzunaProperties properties = new AdzunaProperties();

        properties.setPagesPerSearch(configured);

        assertThat(properties.getPagesPerSearch()).isEqualTo(expected);
    }
}
