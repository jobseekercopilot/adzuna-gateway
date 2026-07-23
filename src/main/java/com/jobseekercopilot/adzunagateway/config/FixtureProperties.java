package com.jobseekercopilot.adzunagateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "fixture")
public class FixtureProperties {
    private String datasetId = "uk-software-developer-demo";
    private String datasetVersion = "1.0.0";
    private String scenario = "DEMO_READY";
    private String systemDataServiceUrl = "http://system-data-service:8103";
    private Latency latency = new Latency();

    public String getDatasetId() { return datasetId; }
    public void setDatasetId(String datasetId) { this.datasetId = datasetId; }
    public String getDatasetVersion() { return datasetVersion; }
    public void setDatasetVersion(String datasetVersion) { this.datasetVersion = datasetVersion; }
    public String getScenario() { return scenario; }
    public void setScenario(String scenario) { this.scenario = scenario; }
    public String getSystemDataServiceUrl() { return systemDataServiceUrl; }
    public void setSystemDataServiceUrl(String systemDataServiceUrl) { this.systemDataServiceUrl = systemDataServiceUrl; }
    public Latency getLatency() { return latency; }
    public void setLatency(Latency latency) { this.latency = latency; }

    public static class Latency {
        private boolean enabled;
        private long jobSearchMs = 0L;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public long getJobSearchMs() { return jobSearchMs; }
        public void setJobSearchMs(long jobSearchMs) { this.jobSearchMs = jobSearchMs; }
    }
}
