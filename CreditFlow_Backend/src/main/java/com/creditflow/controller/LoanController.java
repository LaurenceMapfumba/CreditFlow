package com.creditflow.controller;

import com.creditflow.dto.request.InvestmentRequest;
import com.creditflow.dto.request.LoanApplicationRequest;
import com.creditflow.dto.request.RepaymentRequest;
import com.creditflow.dto.response.ApiResponse;
import com.creditflow.dto.response.LoanInvestmentResponse;
import com.creditflow.dto.response.LoanResponse;
import com.creditflow.security.UserPrincipal;
import com.creditflow.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@Tag(name = "Loans", description = "Loan application and management endpoints")
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/apply")
    @PreAuthorize("hasRole('BORROWER')")
    @Operation(summary = "Apply for a loan (Borrower only)")
    public ResponseEntity<ApiResponse<LoanResponse>> apply(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody LoanApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Loan application submitted", loanService.applyForLoan(principal.getId(), request)));
    }

    @GetMapping("/my-loans")
    @PreAuthorize("hasRole('BORROWER')")
    @Operation(summary = "Get all loans for the current borrower")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> myLoans(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getBorrowerLoans(principal.getId())));
    }

    @GetMapping("/borrower/{userId}")
    @PreAuthorize("hasAnyRole('LENDER', 'INVESTOR', 'ADMIN')")
    @Operation(summary = "Get loan history for a specific borrower (Lender/Investor/Admin only)")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> borrowerLoans(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getBorrowerLoans(userId)));
    }

    @GetMapping("/marketplace")
    @PreAuthorize("hasAnyRole('LENDER', 'INVESTOR', 'ADMIN')")
    @Operation(summary = "Browse approved loans available for investment")
    public ResponseEntity<ApiResponse<Page<LoanResponse>>> marketplace(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getAvailableLoans(pageable)));
    }

    @PostMapping("/invest")
    @PreAuthorize("hasAnyRole('LENDER', 'INVESTOR')")
    @Operation(summary = "Invest in an approved loan")
    public ResponseEntity<ApiResponse<LoanResponse>> invest(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody InvestmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Investment successful", loanService.fundLoan(principal.getId(), request)));
    }

    @GetMapping("/my-investments")
    @PreAuthorize("hasAnyRole('LENDER', 'INVESTOR')")
    @Operation(summary = "Get all investments made by the current lender")
    public ResponseEntity<ApiResponse<List<LoanInvestmentResponse>>> myInvestments(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getMyInvestments(principal.getId())));
    }

    @PostMapping("/repay")
    @PreAuthorize("hasRole('BORROWER')")
    @Operation(summary = "Make a loan repayment")
    public ResponseEntity<ApiResponse<Void>> repay(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody RepaymentRequest request) {
        loanService.makeRepayment(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Repayment processed successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get loan details by ID")
    public ResponseEntity<ApiResponse<LoanResponse>> getLoan(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getLoanById(id)));
    }
}
