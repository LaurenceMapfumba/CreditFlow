package com.creditflow.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_investments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanInvestment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lender_id", nullable = false)
    private User lender;

    @Column(name = "invested_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal investedAmount;

    @Column(name = "expected_return", precision = 15, scale = 2)
    private BigDecimal expectedReturn;

    @Column(name = "actual_return", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal actualReturn = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "invested_at", updatable = false)
    private LocalDateTime investedAt;
}
