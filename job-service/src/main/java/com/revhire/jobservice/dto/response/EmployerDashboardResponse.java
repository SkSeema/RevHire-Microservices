package com.revhire.jobservice.dto.response;
import lombok.Data;

@Data
public class EmployerDashboardResponse {
    private long totalJobs;
    private long activeJobs;
    private long totalApplications;
    private long pendingReviews;
    private long shortlistedCount;
    private long rejectedCount;
}
