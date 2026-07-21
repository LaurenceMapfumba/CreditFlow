package com.creditflow.controller;

import com.creditflow.dto.response.ApiResponse;
import com.creditflow.dto.response.TransactionResponse;
import com.creditflow.security.UserPrincipal;
import com.creditflow.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Wallet and transaction endpoints")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    @Operation(summary = "Deposit funds to your wallet")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam @DecimalMin("1.00") BigDecimal amount) {
        return ResponseEntity.ok(ApiResponse.success("Deposit successful",
                transactionService.depositToWallet(principal.getId(), amount)));
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw funds from your wallet")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam @DecimalMin("1.00") BigDecimal amount) {
        return ResponseEntity.ok(ApiResponse.success("Withdrawal successful",
                transactionService.withdrawFromWallet(principal.getId(), amount)));
    }

    @GetMapping
    @Operation(summary = "Get your transaction history")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> history(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                transactionService.getUserTransactions(principal.getId(), pageable)));
    }
}
