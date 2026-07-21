package com.creditflow.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanApplicationRequest {

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "100.00", message = "Minimum loan amount is $100")
    @DecimalMax(value = "50000.00", message = "Maximum loan amount is $50,000")
    private BigDecimal amount;

    @NotNull(message = "Loan term is required")
    @Min(value = 1, message = "Minimum term is 1 month")
    @Max(value = 36, message = "Maximum term is 36 months")
    private Integer termMonths;

    @NotBlank(message = "Loan purpose is required")
    @Size(min = 20, max = 500, message = "Purpose must be between 20 and 500 characters")
    private String purpose;
}
