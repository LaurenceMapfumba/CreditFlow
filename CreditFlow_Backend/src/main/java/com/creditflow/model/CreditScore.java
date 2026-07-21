package com.creditflow.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_scores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Score range: 300 - 850
    @Column(nullable = false)
    @Builder.Default
    private Integer score = 300;

    // Risk tier: A (lowest risk) to E (highest risk)
    @Column(name = "risk_tier", length = 1)
    private String riskTier;

    @Column(name = "mobile_money_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal mobileMoneyScore = BigDecimal.ZERO;

    @Column(name = "repayment_history_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal repaymentHistoryScore = BigDecimal.ZERO;

    @Column(name = "bank_behaviour_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal bankBehaviourScore = BigDecimal.ZERO;

    @Column(name = "kyc_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal kycScore = BigDecimal.ZERO;

    @Column(name = "total_loans_taken")
    @Builder.Default
    private Integer totalLoansTaken = 0;

    @Column(name = "loans_repaid_on_time")
    @Builder.Default
    private Integer loansRepaidOnTime = 0;

    @Column(name = "loans_defaulted")
    @Builder.Default
    private Integer loansDefaulted = 0;

    @Column(name = "suggested_interest_rate", precision = 5, scale = 2)
    private BigDecimal suggestedInterestRate;

    @UpdateTimestamp
    @Column(name = "last_calculated_at")
    private LocalDateTime lastCalculatedAt;
}
