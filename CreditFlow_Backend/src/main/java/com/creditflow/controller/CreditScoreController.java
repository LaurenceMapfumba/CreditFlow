package com.creditflow.controller;

import com.creditflow.dto.response.ApiResponse;
import com.creditflow.dto.response.CreditScoreResponse;
import com.creditflow.security.UserPrincipal;
import com.creditflow.service.CreditScoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/credit-score")
@RequiredArgsConstructor
@Tag(name = "Credit Score", description = "Credit scoring endpoints")
public class CreditScoreController {

    private final CreditScoringService creditScoringService;

    @GetMapping("/my-score")
    @Operation(summary = "Get your current credit score")
    public ResponseEntity<ApiResponse<CreditScoreResponse>> getMyScore(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(creditScoringService.getScore(principal.getId())));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('LENDER', 'INVESTOR', 'ADMIN')")
    @Operation(summary = "Get credit score for a specific borrower (Lender/Investor/Admin only)")
    public ResponseEntity<ApiResponse<CreditScoreResponse>> getScoreForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(creditScoringService.getScore(userId)));
    }

    @PostMapping("/recalculate")
    @Operation(summary = "Recalculate your credit score based on latest activity")
    public ResponseEntity<ApiResponse<CreditScoreResponse>> recalculate(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Credit score updated",
                creditScoringService.calculateAndUpdateScore(principal.getId())));
    }
}
