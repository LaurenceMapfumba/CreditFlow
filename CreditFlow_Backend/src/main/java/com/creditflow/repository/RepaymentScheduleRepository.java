package com.creditflow.repository;

import com.creditflow.model.RepaymentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RepaymentScheduleRepository extends JpaRepository<RepaymentSchedule, Long> {
    List<RepaymentSchedule> findByLoanIdOrderByInstallmentNumber(Long loanId);
    List<RepaymentSchedule> findByIsPaidFalseAndDueDateBefore(LocalDate date);

    @Query("SELECT r FROM RepaymentSchedule r WHERE r.loan.id = :loanId AND r.isPaid = false ORDER BY r.installmentNumber ASC")
    List<RepaymentSchedule> findUnpaidByLoanId(Long loanId);
}
