package com.creditflow.service;

import com.creditflow.dto.response.TransactionResponse;
import com.creditflow.exception.BadRequestException;
import com.creditflow.exception.ResourceNotFoundException;
import com.creditflow.model.Transaction;
import com.creditflow.model.User;
import com.creditflow.model.enums.TransactionStatus;
import com.creditflow.model.enums.TransactionType;
import com.creditflow.repository.TransactionRepository;
import com.creditflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional
    public TransactionResponse depositToWallet(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Deposit amount must be positive");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        user.setWalletBalance(user.getWalletBalance().add(amount));
        userRepository.save(user);

        Transaction tx = Transaction.builder()
                .reference(UUID.randomUUID().toString())
                .recipient(user)
                .amount(amount)
                .type(TransactionType.WALLET_DEPOSIT)
                .status(TransactionStatus.COMPLETED)
                .description("Wallet deposit")
                .build();

        return toResponse(transactionRepository.save(tx));
    }

    @Transactional
    public TransactionResponse withdrawFromWallet(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Withdrawal amount must be positive");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (user.getWalletBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient wallet balance");
        }

        user.setWalletBalance(user.getWalletBalance().subtract(amount));
        userRepository.save(user);

        Transaction tx = Transaction.builder()
                .reference(UUID.randomUUID().toString())
                .sender(user)
                .amount(amount)
                .type(TransactionType.WALLET_WITHDRAWAL)
                .status(TransactionStatus.COMPLETED)
                .description("Wallet withdrawal")
                .build();

        return toResponse(transactionRepository.save(tx));
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getUserTransactions(Long userId, Pageable pageable) {
        return transactionRepository.findBySenderIdOrRecipientId(userId, userId, pageable)
                .map(this::toResponse);
    }

    private TransactionResponse toResponse(Transaction tx) {
        return TransactionResponse.builder()
                .id(tx.getId())
                .reference(tx.getReference())
                .amount(tx.getAmount())
                .type(tx.getType())
                .status(tx.getStatus())
                .paymentMethod(tx.getPaymentMethod())
                .description(tx.getDescription())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}
