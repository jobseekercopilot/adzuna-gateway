package com.jobseekercopilot.adzunagateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "adzuna")
public class AdzunaProperties {
    public static final int DEFAULT_RESULTS_PER_PAGE = 50;
    public static final int MAX_RESULTS_PER_PAGE = 50;
    public static final int DEFAULT_PAGES_PER_SEARCH = 1;
    public static final int MAX_PAGES_PER_SEARCH = 2;

    private String baseUrl;
    private String appId;
    private String appKey;
    private String country;
    private int resultsPerPage = DEFAULT_RESULTS_PER_PAGE;
    private int pagesPerSearch = DEFAULT_PAGES_PER_SEARCH;
    private boolean enabled;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getAppKey() { return appKey; }
    public void setAppKey(String appKey) { this.appKey = appKey; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public int getResultsPerPage() { return resultsPerPage; }
    public void setResultsPerPage(int resultsPerPage) {
        this.resultsPerPage = clampResultsPerPage(resultsPerPage);
    }
    public int getPagesPerSearch() { return pagesPerSearch; }
    public void setPagesPerSearch(int pagesPerSearch) {
        this.pagesPerSearch = Math.max(
                DEFAULT_PAGES_PER_SEARCH,
                Math.min(pagesPerSearch, MAX_PAGES_PER_SEARCH));
    }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public static int clampResultsPerPage(int resultsPerPage) {
        return Math.max(
                1,
                Math.min(resultsPerPage, MAX_RESULTS_PER_PAGE));
    }
}
