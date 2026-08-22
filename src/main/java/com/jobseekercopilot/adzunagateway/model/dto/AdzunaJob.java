package com.jobseekercopilot.adzunagateway.model.dto;

import java.math.BigDecimal;
import java.util.List;

public class AdzunaJob {
    private String externalJobId, title, companyName, description, locationDisplayName, contractType, employmentType, remoteType, category, postedAt, redirectUrl;
    private List<String> locationAreas;
    private BigDecimal latitude, longitude;
    private Integer salaryMinimum, salaryMaximum;
    private Boolean salaryPredicted;
    public String getExternalJobId() { return externalJobId; }
    public void setExternalJobId(String externalJobId) { this.externalJobId = externalJobId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocationDisplayName() { return locationDisplayName; }
    public void setLocationDisplayName(String locationDisplayName) { this.locationDisplayName = locationDisplayName; }
    public String getContractType() { return contractType; }
    public void setContractType(String contractType) { this.contractType = contractType; }
    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }
    public String getRemoteType() { return remoteType; }
    public void setRemoteType(String remoteType) { this.remoteType = remoteType; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPostedAt() { return postedAt; }
    public void setPostedAt(String postedAt) { this.postedAt = postedAt; }
    public String getRedirectUrl() { return redirectUrl; }
    public void setRedirectUrl(String redirectUrl) { this.redirectUrl = redirectUrl; }
    public List<String> getLocationAreas() { return locationAreas; }
    public void setLocationAreas(List<String> locationAreas) { this.locationAreas = locationAreas; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public Integer getSalaryMinimum() { return salaryMinimum; }
    public void setSalaryMinimum(Integer salaryMinimum) { this.salaryMinimum = salaryMinimum; }
    public Integer getSalaryMaximum() { return salaryMaximum; }
    public void setSalaryMaximum(Integer salaryMaximum) { this.salaryMaximum = salaryMaximum; }
    public Boolean getSalaryPredicted() { return salaryPredicted; }
    public void setSalaryPredicted(Boolean salaryPredicted) { this.salaryPredicted = salaryPredicted; }
}
