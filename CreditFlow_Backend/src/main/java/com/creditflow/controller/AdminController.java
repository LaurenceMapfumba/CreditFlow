package com.creditflow.controller;

import com.creditflow.dto.request.CreditScoreOverrideRequest;
import com.creditflow.dto.request.LoanReviewRequest;
import com.creditflow.dto.response.ApiResponse;
import com.creditflow.dto.response.CreditScoreResponse;
import com.creditflow.dto.response.KycDocumentResponse;
import com.creditflow.dto.response.LoanResponse;
import com.creditflow.dto.response.UserResponse;
import com.creditflow.model.enums.KycStatus;
import com.creditflow.model.enums.LoanStatus;
import com.creditflow.security.UserPrincipal;
import com.creditflow.service.CreditScoringService;
import com.creditflow.service.KycService;
import com.creditflow.service.LoanService;
import com.creditflow.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin panel - User management, loan monitoring, KYC review")
public class AdminController {

    private final UserService userService;
    private final LoanService loanService;
    private final KycService kycService;
    private final CreditScoringService creditScoringService;

    // --- User Management ---

    @GetMapping("/users")
    @Operation(summary = "List all users (paginated)")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> listUsers(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers(pageable)));
    }

    @PatchMapping("/users/{id}/deactivate")
    @Operation(summary = "Deactivate a user account")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deactivated"));
    }

    @PatchMapping("/users/{id}/activate")
    @Operation(summary = "Activate a user account")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User activated"));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Soft-delete a user account (sets is_deleted=true, is_active=false)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.softDeleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted"));
    }

    @PatchMapping("/users/{id}/restore")
    @Operation(summary = "Restore a soft-deleted user account")
    public ResponseEntity<ApiResponse<Void>> restoreUser(@PathVariable Long id) {
        userService.restoreUser(id);
        return ResponseEntity.ok(ApiResponse.success("User restored"));
    }

    // --- Credit Score Management ---

    @PatchMapping("/credit-score/{userId}")
    @Operation(summary = "Override credit score fields for a user")
    public ResponseEntity<ApiResponse<CreditScoreResponse>> overrideCreditScore(
            @PathVariable Long userId,
            @RequestBody CreditScoreOverrideRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Credit score updated",
                creditScoringService.overrideScore(userId, request)));
    }

    // --- Loan Monitoring ---

    @GetMapping("/loans/pending")
    @Operation(summary = "List all loans pending review")
    public ResponseEntity<ApiResponse<Page<LoanResponse>>> pendingLoans(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getAvailableLoans(pageable)));
    }

    @PatchMapping("/loans/{id}/review")
    @Operation(summary = "Approve or reject a loan application")
    public ResponseEntity<ApiResponse<LoanResponse>> reviewLoan(
            @PathVariable Long id,
            @Valid @RequestBody LoanReviewRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Loan reviewed",
                loanService.reviewLoan(id, request, principal.getId())));
    }

    // --- KYC Review ---

    @GetMapping("/kyc/pending")
    @Operation(summary = "List all KYC submissions pending review")
    public ResponseEntity<ApiResponse<List<KycDocumentResponse>>> pendingKyc() {
        return ResponseEntity.ok(ApiResponse.success(kycService.getPendingKycDocuments()));
    }

    @PatchMapping("/kyc/{id}/approve")
    @Operation(summary = "Approve a KYC submission")
    public ResponseEntity<ApiResponse<Void>> approveKyc(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        kycService.reviewKyc(id, KycStatus.APPROVED, null, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("KYC approved"));
    }

    @PatchMapping("/kyc/{id}/reject")
    @Operation(summary = "Reject a KYC submission")
    public ResponseEntity<ApiResponse<Void>> rejectKyc(
            @PathVariable Long id,
            @RequestParam String reason,
            @AuthenticationPrincipal UserPrincipal principal) {
        kycService.reviewKyc(id, KycStatus.REJECTED, reason, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("KYC rejected"));
    }
}
