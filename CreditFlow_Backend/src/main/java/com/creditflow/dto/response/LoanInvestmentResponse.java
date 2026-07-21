package com.creditflow.dto.response;

import com.creditflow.model.enums.LoanStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanInvestmentResponse {
    private Long id;
    private Long loanId;
    private String borrowerName;
    private String purpose;
    private LoanStatus loanStatus;
    private BigDecimal principalAmount;
    private BigDecimal investedAmount;
    private BigDecimal expectedReturn;
    private BigDecimal actualReturn;
    private BigDecimal interestRate;
    private Integer termMonths;
    private BigDecimal amountRepaid;
    private LocalDate dueDate;
    private LocalDateTime investedAt;
}
