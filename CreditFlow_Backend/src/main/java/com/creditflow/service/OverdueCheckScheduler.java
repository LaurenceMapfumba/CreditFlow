package com.creditflow.service;

import com.creditflow.model.Loan;
import com.creditflow.model.RepaymentSchedule;
import com.creditflow.model.enums.LoanStatus;
import com.creditflow.repository.LoanRepository;
import com.creditflow.repository.RepaymentScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OverdueCheckScheduler {

    private static final Logger log = LoggerFactory.getLogger(OverdueCheckScheduler.class);

    private final RepaymentScheduleRepository repaymentScheduleRepository;
    private final LoanRepository loanRepository;
    private final NotificationService notificationService;

    // Runs daily at midnight
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void checkOverdueInstallments() {
        log.info("Running overdue installment check...");
        List<RepaymentSchedule> overdue = repaymentScheduleRepository
                .findByIsPaidFalseAndDueDateBefore(LocalDate.now());

        for (RepaymentSchedule schedule : overdue) {
            if (!schedule.isOverdue()) {
                schedule.setOverdue(true);
                Loan loan = schedule.getLoan();
                notificationService.notify(loan.getBorrower(),
                        "Overdue Repayment",
                        "Installment #" + schedule.getInstallmentNumber() +
                                " for loan #" + loan.getId() + " was due on " +
                                schedule.getDueDate() + " and is now overdue.");
            }
        }
        repaymentScheduleRepository.saveAll(overdue);
        log.info("Overdue check complete. Flagged {} installments.", overdue.size());
    }

    // Mark loans with all installments overdue as defaulted
    @Scheduled(cron = "0 30 0 * * *")
    @Transactional
    public void markDefaultedLoans() {
        List<Loan> activeLoans = loanRepository.findByBorrowerIdAndStatus(0L, LoanStatus.ACTIVE);
        // Simplified: loans past due date by 30+ days with no repayments are flagged
        LocalDate cutoff = LocalDate.now().minusDays(30);
        loanRepository.findAll().stream()
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE &&
                        l.getDueDate() != null &&
                        l.getDueDate().isBefore(cutoff) &&
                        l.getAmountRepaid().compareTo(l.getTotalRepayment()) < 0)
                .forEach(loan -> {
                    loan.setStatus(LoanStatus.DEFAULTED);
                    loanRepository.save(loan);
                    notificationService.notify(loan.getBorrower(),
                            "Loan Defaulted",
                            "Loan #" + loan.getId() + " has been marked as defaulted due to non-payment.");
                    log.warn("Loan {} marked as DEFAULTED", loan.getId());
                });
    }
}
