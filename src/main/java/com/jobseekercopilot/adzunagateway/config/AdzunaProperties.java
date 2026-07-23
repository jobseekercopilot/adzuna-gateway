package com.jobseekercopilot.adzunagateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "adzuna")
public class AdzunaProperties {
    private String baseUrl;
    private String appId;
    private String appKey;
    private String country;
    private int resultsPerPage;
    private int pagesPerSearch;
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
    public void setResultsPerPage(int resultsPerPage) { this.resultsPerPage = resultsPerPage; }
    public int getPagesPerSearch() { return pagesPerSearch; }
    public void setPagesPerSearch(int pagesPerSearch) { this.pagesPerSearch = pagesPerSearch; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
