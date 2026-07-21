package com.creditflow.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvestmentRequest {

    @NotNull(message = "Loan ID is required")
    private Long loanId;

    @NotNull(message = "Investment amount is required")
    @DecimalMin(value = "50.00", message = "Minimum investment amount is $50")
    private BigDecimal amount;
}
