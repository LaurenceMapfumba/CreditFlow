package com.creditflow.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CreditScoreResponse {
    private Long userId;
    private Integer score;
    private String riskTier;
    private BigDecimal suggestedInterestRate;
    private BigDecimal mobileMoneyScore;
    private BigDecimal repaymentHistoryScore;
    private BigDecimal bankBehaviourScore;
    private BigDecimal kycScore;
    private Integer totalLoansTaken;
    private Integer loansRepaidOnTime;
    private Integer loansDefaulted;
    private LocalDateTime lastCalculatedAt;
}
