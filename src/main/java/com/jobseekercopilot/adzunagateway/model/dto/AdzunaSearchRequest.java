package com.jobseekercopilot.adzunagateway.model.dto;

public class AdzunaSearchRequest {
    private String targetRole;
    private String location;
    private Integer distanceMiles;
    private Integer page;
    private Integer resultsPerPage;
    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getDistanceMiles() { return distanceMiles; }
    public void setDistanceMiles(Integer distanceMiles) { this.distanceMiles = distanceMiles; }
    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    public Integer getResultsPerPage() { return resultsPerPage; }
    public void setResultsPerPage(Integer resultsPerPage) { this.resultsPerPage = resultsPerPage; }
}
