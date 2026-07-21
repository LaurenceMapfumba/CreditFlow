package com.creditflow.repository;

import com.creditflow.model.LoanInvestment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanInvestmentRepository extends JpaRepository<LoanInvestment, Long> {
    List<LoanInvestment> findByLenderId(Long lenderId);
    List<LoanInvestment> findByLoanId(Long loanId);
    boolean existsByLoanIdAndLenderId(Long loanId, Long lenderId);
}
