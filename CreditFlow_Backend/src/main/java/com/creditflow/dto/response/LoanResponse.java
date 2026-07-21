package com.creditflow.dto.response;

import com.creditflow.model.enums.LoanStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanResponse {
    private Long id;
    private Long borrowerId;
    private String borrowerName;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer termMonths;
    private BigDecimal monthlyRepayment;
    private BigDecimal totalRepayment;
    private BigDecimal amountRepaid;
    private BigDecimal fundedAmount;
    private BigDecimal remainingBalance;
    private LoanStatus status;
    private String purpose;
    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
}
