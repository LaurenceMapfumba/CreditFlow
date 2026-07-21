package com.creditflow.service;

import com.creditflow.dto.request.CreditScoreOverrideRequest;
import com.creditflow.dto.response.CreditScoreResponse;
import com.creditflow.exception.ResourceNotFoundException;
import com.creditflow.model.CreditScore;
import com.creditflow.model.User;
import com.creditflow.model.enums.KycStatus;
import com.creditflow.repository.CreditScoreRepository;
import com.creditflow.repository.LoanRepository;
import com.creditflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class CreditScoringService {

    private final CreditScoreRepository creditScoreRepository;
    private final UserRepository userRepository;
    private final LoanRepository loanRepository;

    // Score weights
    private static final BigDecimal REPAYMENT_WEIGHT = new BigDecimal("0.40");
    private static final BigDecimal MOBILE_MONEY_WEIGHT = new BigDecimal("0.30");
    private static final BigDecimal KYC_WEIGHT = new BigDecimal("0.20");
    private static final BigDecimal BANK_WEIGHT = new BigDecimal("0.10");

    @Transactional
    public CreditScoreResponse calculateAndUpdateScore(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        CreditScore creditScore = creditScoreRepository.findByUserId(userId)
                .orElse(CreditScore.builder().user(user).build());

        long totalLoans = loanRepository.countRepaidByBorrower(userId)
                + loanRepository.countDefaultedByBorrower(userId);
        long repaidOnTime = loanRepository.countRepaidByBorrower(userId);
        long defaulted = loanRepository.countDefaultedByBorrower(userId);

        BigDecimal repaymentScore = calculateRepaymentScore(totalLoans, repaidOnTime, defaulted);
        BigDecimal kycScore = calculateKycScore(user.getKycStatus());

        // Mobile money and bank scores need external API integration - using defaults for now
        BigDecimal mobileMoneyScore = creditScore.getMobileMoneyScore().compareTo(BigDecimal.ZERO) > 0
                ? creditScore.getMobileMoneyScore()
                : new BigDecimal("50");
        BigDecimal bankScore = creditScore.getBankBehaviourScore().compareTo(BigDecimal.ZERO) > 0
                ? creditScore.getBankBehaviourScore()
                : new BigDecimal("50");

        BigDecimal compositeScore = repaymentScore.multiply(REPAYMENT_WEIGHT)
                .add(mobileMoneyScore.multiply(MOBILE_MONEY_WEIGHT))
                .add(kycScore.multiply(KYC_WEIGHT))
                .add(bankScore.multiply(BANK_WEIGHT));

        // Map composite (0-100) to credit score range (300-850)
        int finalScore = 300 + compositeScore.multiply(new BigDecimal("5.5")).intValue();
        finalScore = Math.min(finalScore, 850);

        creditScore.setScore(finalScore);
        creditScore.setRiskTier(determineRiskTier(finalScore));
        creditScore.setRepaymentHistoryScore(repaymentScore);
        creditScore.setMobileMoneyScore(mobileMoneyScore);
        creditScore.setKycScore(kycScore);
        creditScore.setBankBehaviourScore(bankScore);
        creditScore.setTotalLoansTaken((int) totalLoans);
        creditScore.setLoansRepaidOnTime((int) repaidOnTime);
        creditScore.setLoansDefaulted((int) defaulted);
        creditScore.setSuggestedInterestRate(calculateInterestRate(creditScore.getRiskTier()));

        creditScore = creditScoreRepository.save(creditScore);
        return toResponse(creditScore, userId);
    }

    @Transactional(readOnly = true)
    public CreditScoreResponse getScore(Long userId) {
        CreditScore score = creditScoreRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Credit score not found for user: " + userId));
        return toResponse(score, userId);
    }

    @Transactional
    public CreditScoreResponse overrideScore(Long userId, CreditScoreOverrideRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        CreditScore score = creditScoreRepository.findByUserId(userId)
                .orElse(CreditScore.builder().user(user).build());

        if (req.getScore() != null)
            score.setScore(Math.max(300, Math.min(850, req.getScore())));
        if (req.getRiskTier() != null)
            score.setRiskTier(req.getRiskTier());
        if (req.getSuggestedInterestRate() != null)
            score.setSuggestedInterestRate(req.getSuggestedInterestRate());
        if (req.getMobileMoneyScore() != null)
            score.setMobileMoneyScore(req.getMobileMoneyScore());
        if (req.getRepaymentHistoryScore() != null)
            score.setRepaymentHistoryScore(req.getRepaymentHistoryScore());
        if (req.getBankBehaviourScore() != null)
            score.setBankBehaviourScore(req.getBankBehaviourScore());
        if (req.getKycScore() != null)
            score.setKycScore(req.getKycScore());

        return toResponse(creditScoreRepository.save(score), userId);
    }

    private BigDecimal calculateRepaymentScore(long total, long repaid, long defaulted) {
        if (total == 0) return new BigDecimal("50"); // neutral for new users
        double rate = (double) repaid / total;
        double penalty = (double) defaulted / total * 30;
        return BigDecimal.valueOf(Math.max(0, rate * 100 - penalty)).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateKycScore(KycStatus status) {
        return switch (status) {
            case APPROVED -> new BigDecimal("100");
            case PENDING_REVIEW -> new BigDecimal("50");
            case NOT_SUBMITTED, REJECTED -> BigDecimal.ZERO;
        };
    }

    private String determineRiskTier(int score) {
        if (score >= 750) return "A";
        if (score >= 650) return "B";
        if (score >= 550) return "C";
        if (score >= 450) return "D";
        return "E";
    }

    private BigDecimal calculateInterestRate(String riskTier) {
        return switch (riskTier) {
            case "A" -> new BigDecimal("8.00");
            case "B" -> new BigDecimal("12.00");
            case "C" -> new BigDecimal("16.00");
            case "D" -> new BigDecimal("22.00");
            default -> new BigDecimal("30.00");
        };
    }

    private CreditScoreResponse toResponse(CreditScore cs, Long userId) {
        return CreditScoreResponse.builder()
                .userId(userId)
                .score(cs.getScore())
                .riskTier(cs.getRiskTier())
                .suggestedInterestRate(cs.getSuggestedInterestRate())
                .mobileMoneyScore(cs.getMobileMoneyScore())
                .repaymentHistoryScore(cs.getRepaymentHistoryScore())
                .bankBehaviourScore(cs.getBankBehaviourScore())
                .kycScore(cs.getKycScore())
                .totalLoansTaken(cs.getTotalLoansTaken())
                .loansRepaidOnTime(cs.getLoansRepaidOnTime())
                .loansDefaulted(cs.getLoansDefaulted())
                .lastCalculatedAt(cs.getLastCalculatedAt())
                .build();
    }
}
