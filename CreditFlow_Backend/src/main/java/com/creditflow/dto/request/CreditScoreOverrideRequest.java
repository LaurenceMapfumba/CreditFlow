package com.creditflow.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreditScoreOverrideRequest {
    private Integer score;
    private String riskTier;
    private BigDecimal suggestedInterestRate;
    private BigDecimal mobileMoneyScore;
    private BigDecimal repaymentHistoryScore;
    private BigDecimal bankBehaviourScore;
    private BigDecimal kycScore;
}
