package com.creditflow.dto.request;

import com.creditflow.model.enums.LoanStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoanReviewRequest {

    @NotNull(message = "Status is required")
    private LoanStatus status;

    private String adminNotes;
}
