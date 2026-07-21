package com.creditflow.repository;

import com.creditflow.model.Loan;
import com.creditflow.model.enums.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByBorrowerId(Long borrowerId);
    Page<Loan> findByStatus(LoanStatus status, Pageable pageable);
    List<Loan> findByBorrowerIdAndStatus(Long borrowerId, LoanStatus status);

    @Query("SELECT l FROM Loan l WHERE l.status = 'APPROVED' AND l.fundedAmount < l.principalAmount")
    Page<Loan> findAvailableForInvestment(Pageable pageable);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.borrower.id = :borrowerId AND l.status = 'DEFAULTED'")
    long countDefaultedByBorrower(@Param("borrowerId") Long borrowerId);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.borrower.id = :borrowerId AND l.status = 'REPAID'")
    long countRepaidByBorrower(@Param("borrowerId") Long borrowerId);
}
