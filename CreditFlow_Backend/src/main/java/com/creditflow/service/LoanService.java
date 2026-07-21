package com.creditflow.service;

import com.creditflow.dto.request.InvestmentRequest;
import com.creditflow.dto.request.LoanApplicationRequest;
import com.creditflow.dto.request.LoanReviewRequest;
import com.creditflow.dto.request.RepaymentRequest;
import com.creditflow.dto.response.LoanInvestmentResponse;
import com.creditflow.dto.response.LoanResponse;
import com.creditflow.exception.BadRequestException;
import com.creditflow.exception.ResourceNotFoundException;
import com.creditflow.model.*;
import com.creditflow.model.enums.KycStatus;
import com.creditflow.model.enums.LoanStatus;
import com.creditflow.model.enums.TransactionType;
import com.creditflow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final CreditScoreRepository creditScoreRepository;
    private final LoanInvestmentRepository investmentRepository;
    private final RepaymentScheduleRepository repaymentScheduleRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;

    @Transactional
    public LoanResponse applyForLoan(Long borrowerId, LoanApplicationRequest request) {
        User borrower = userRepository.findById(borrowerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", borrowerId));

        if (borrower.getKycStatus() != KycStatus.APPROVED) {
            throw new BadRequestException("KYC verification must be approved before applying for a loan");
        }

        CreditScore score = creditScoreRepository.findByUserId(borrowerId)
                .orElseThrow(() -> new BadRequestException("No credit score found. Please complete KYC first."));

        BigDecimal interestRate = score.getSuggestedInterestRate() != null
                ? score.getSuggestedInterestRate()
                : new BigDecimal("20.00");

        BigDecimal monthly = calculateMonthlyRepayment(request.getAmount(), interestRate, request.getTermMonths());
        BigDecimal total = monthly.multiply(BigDecimal.valueOf(request.getTermMonths())).setScale(2, RoundingMode.HALF_UP);

        Loan loan = Loan.builder()
                .borrower(borrower)
                .principalAmount(request.getAmount())
                .interestRate(interestRate)
                .termMonths(request.getTermMonths())
                .monthlyRepayment(monthly)
                .totalRepayment(total)
                .purpose(request.getPurpose())
                .status(LoanStatus.PENDING)
                .build();

        loan = loanRepository.save(loan);

        notificationService.notify(borrower, "Loan Application Submitted",
                "Your loan application for $" + request.getAmount() + " is under review.");

        return toResponse(loan);
    }

    @Transactional
    public LoanResponse reviewLoan(Long loanId, LoanReviewRequest request, Long adminId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", loanId));

        if (loan.getStatus() != LoanStatus.PENDING && loan.getStatus() != LoanStatus.UNDER_REVIEW) {
            throw new BadRequestException("Only PENDING or UNDER_REVIEW loans can be reviewed");
        }

        loan.setStatus(request.getStatus());
        loan.setAdminNotes(request.getAdminNotes());
        loan = loanRepository.save(loan);

        String message = request.getStatus() == LoanStatus.APPROVED
                ? "Your loan application has been approved and is now open for funding."
                : "Your loan application has been rejected. Reason: " + request.getAdminNotes();

        notificationService.notify(loan.getBorrower(), "Loan Application Update", message);
        return toResponse(loan);
    }

    @Transactional
    public LoanResponse fundLoan(Long lenderId, InvestmentRequest request) {
        User lender = userRepository.findById(lenderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", lenderId));
        Loan loan = loanRepository.findById(request.getLoanId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan", request.getLoanId()));

        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new BadRequestException("Loan is not available for investment");
        }

        BigDecimal remaining = loan.getPrincipalAmount().subtract(loan.getFundedAmount());
        if (request.getAmount().compareTo(remaining) > 0) {
            throw new BadRequestException("Investment amount exceeds the remaining unfunded amount of $" + remaining);
        }

        if (lender.getWalletBalance().compareTo(request.getAmount()) < 0) {
            throw new BadRequestException("Insufficient wallet balance");
        }

        lender.setWalletBalance(lender.getWalletBalance().subtract(request.getAmount()));
        userRepository.save(lender);

        BigDecimal expectedReturn = request.getAmount()
                .multiply(BigDecimal.ONE.add(loan.getInterestRate().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)));

        LoanInvestment investment = LoanInvestment.builder()
                .loan(loan)
                .lender(lender)
                .investedAmount(request.getAmount())
                .expectedReturn(expectedReturn.setScale(2, RoundingMode.HALF_UP))
                .build();
        investmentRepository.save(investment);

        loan.setFundedAmount(loan.getFundedAmount().add(request.getAmount()));
        if (loan.getFundedAmount().compareTo(loan.getPrincipalAmount()) >= 0) {
            loan.setStatus(LoanStatus.FUNDED);
            disburseLoan(loan);
        }
        loanRepository.save(loan);

        Transaction tx = Transaction.builder()
                .reference(UUID.randomUUID().toString())
                .sender(lender)
                .recipient(loan.getBorrower())
                .loan(loan)
                .amount(request.getAmount())
                .type(TransactionType.LOAN_DISBURSEMENT)
                .description("Investment in loan #" + loan.getId())
                .build();
        transactionRepository.save(tx);

        notificationService.notify(lender, "Investment Confirmed",
                "You have successfully invested $" + request.getAmount() + " in loan #" + loan.getId());

        return toResponse(loan);
    }

    @Transactional
    public void makeRepayment(Long borrowerId, RepaymentRequest request) {
        User borrower = userRepository.findById(borrowerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", borrowerId));
        Loan loan = loanRepository.findById(request.getLoanId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan", request.getLoanId()));

        if (!loan.getBorrower().getId().equals(borrowerId)) {
            throw new BadRequestException("This loan does not belong to you");
        }
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new BadRequestException("Loan is not in ACTIVE status");
        }

        List<RepaymentSchedule> unpaid = repaymentScheduleRepository.findUnpaidByLoanId(loan.getId());
        if (unpaid.isEmpty()) {
            throw new BadRequestException("No outstanding installments found");
        }

        BigDecimal remaining = request.getAmount();
        for (RepaymentSchedule installment : unpaid) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal due = installment.getTotalAmount().subtract(installment.getPaidAmount());
            BigDecimal pay = remaining.min(due);
            installment.setPaidAmount(installment.getPaidAmount().add(pay));
            remaining = remaining.subtract(pay);
            if (installment.getPaidAmount().compareTo(installment.getTotalAmount()) >= 0) {
                installment.setPaid(true);
                installment.setPaidDate(LocalDate.now());
            }
        }
        repaymentScheduleRepository.saveAll(unpaid);

        BigDecimal paid = request.getAmount().subtract(remaining);
        loan.setAmountRepaid(loan.getAmountRepaid().add(paid));
        if (loan.getAmountRepaid().compareTo(loan.getTotalRepayment()) >= 0) {
            loan.setStatus(LoanStatus.REPAID);
        }
        loanRepository.save(loan);

        Transaction tx = Transaction.builder()
                .reference(UUID.randomUUID().toString())
                .sender(borrower)
                .loan(loan)
                .amount(paid)
                .type(TransactionType.LOAN_REPAYMENT)
                .paymentMethod(request.getPaymentMethod())
                .description("Repayment for loan #" + loan.getId())
                .build();
        transactionRepository.save(tx);

        notificationService.notify(borrower, "Repayment Received",
                "Payment of $" + paid + " received for loan #" + loan.getId());
    }

    @Transactional(readOnly = true)
    public Page<LoanResponse> getAvailableLoans(Pageable pageable) {
        return loanRepository.findAvailableForInvestment(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> getBorrowerLoans(Long borrowerId) {
        return loanRepository.findByBorrowerId(borrowerId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public LoanResponse getLoanById(Long loanId) {
        return toResponse(loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", loanId)));
    }

    @Transactional(readOnly = true)
    public List<LoanInvestmentResponse> getMyInvestments(Long lenderId) {
        return investmentRepository.findByLenderId(lenderId)
                .stream()
                .map(this::toInvestmentResponse)
                .toList();
    }

    private LoanInvestmentResponse toInvestmentResponse(LoanInvestment inv) {
        Loan loan = inv.getLoan();
        String borrowerName = loan.getBorrower().getFirstName() + " " + loan.getBorrower().getLastName();
        return LoanInvestmentResponse.builder()
                .id(inv.getId())
                .loanId(loan.getId())
                .borrowerName(borrowerName)
                .purpose(loan.getPurpose())
                .loanStatus(loan.getStatus())
                .principalAmount(loan.getPrincipalAmount())
                .investedAmount(inv.getInvestedAmount())
                .expectedReturn(inv.getExpectedReturn())
                .actualReturn(inv.getActualReturn())
                .interestRate(loan.getInterestRate())
                .termMonths(loan.getTermMonths())
                .amountRepaid(loan.getAmountRepaid())
                .dueDate(loan.getDueDate())
                .investedAt(inv.getInvestedAt())
                .build();
    }

    private void disburseLoan(Loan loan) {
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setStartDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusMonths(loan.getTermMonths()));
        generateRepaymentSchedule(loan);
        notificationService.notify(loan.getBorrower(), "Loan Disbursed",
                "Your loan of $" + loan.getPrincipalAmount() + " has been disbursed. First repayment due: " +
                        LocalDate.now().plusMonths(1));
    }

    private void generateRepaymentSchedule(Loan loan) {
        List<RepaymentSchedule> schedules = new ArrayList<>();
        BigDecimal monthlyRate = loan.getInterestRate()
                .divide(new BigDecimal("1200"), 10, RoundingMode.HALF_UP);

        for (int i = 1; i <= loan.getTermMonths(); i++) {
            BigDecimal interest = loan.getPrincipalAmount()
                    .subtract(loan.getMonthlyRepayment().multiply(BigDecimal.valueOf(i - 1)).subtract(
                            // simplified: use fixed monthly breakdown
                            BigDecimal.ZERO))
                    .multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);

            BigDecimal principal = loan.getMonthlyRepayment().subtract(interest);

            schedules.add(RepaymentSchedule.builder()
                    .loan(loan)
                    .installmentNumber(i)
                    .dueDate(LocalDate.now().plusMonths(i))
                    .principalComponent(principal.max(BigDecimal.ZERO))
                    .interestComponent(interest.max(BigDecimal.ZERO))
                    .totalAmount(loan.getMonthlyRepayment())
                    .build());
        }
        repaymentScheduleRepository.saveAll(schedules);
    }

    private BigDecimal calculateMonthlyRepayment(BigDecimal principal, BigDecimal annualRate, int termMonths) {
        if (annualRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
        }
        BigDecimal r = annualRate.divide(new BigDecimal("1200"), 10, RoundingMode.HALF_UP);
        BigDecimal onePlusR = BigDecimal.ONE.add(r);
        BigDecimal power = onePlusR.pow(termMonths, new MathContext(10));
        return principal.multiply(r).multiply(power)
                .divide(power.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
    }

    private LoanResponse toResponse(Loan loan) {
        BigDecimal remaining = loan.getTotalRepayment() != null
                ? loan.getTotalRepayment().subtract(loan.getAmountRepaid())
                : BigDecimal.ZERO;
        String borrowerName = loan.getBorrower().getFirstName() + " " + loan.getBorrower().getLastName();

        return LoanResponse.builder()
                .id(loan.getId())
                .borrowerId(loan.getBorrower().getId())
                .borrowerName(borrowerName)
                .principalAmount(loan.getPrincipalAmount())
                .interestRate(loan.getInterestRate())
                .termMonths(loan.getTermMonths())
                .monthlyRepayment(loan.getMonthlyRepayment())
                .totalRepayment(loan.getTotalRepayment())
                .amountRepaid(loan.getAmountRepaid())
                .fundedAmount(loan.getFundedAmount())
                .remainingBalance(remaining)
                .status(loan.getStatus())
                .purpose(loan.getPurpose())
                .startDate(loan.getStartDate())
                .dueDate(loan.getDueDate())
                .createdAt(loan.getCreatedAt())
                .build();
    }
}
