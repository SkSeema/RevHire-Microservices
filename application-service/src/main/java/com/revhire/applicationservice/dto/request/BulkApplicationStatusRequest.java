package com.revhire.applicationservice.dto.request;

import com.revhire.applicationservice.model.Application.ApplicationStatus;
import lombok.Data;
import java.util.List;

@Data
public class BulkApplicationStatusRequest {
    private List<Long> applicationIds;
    private ApplicationStatus status;
    private String comment;
}
