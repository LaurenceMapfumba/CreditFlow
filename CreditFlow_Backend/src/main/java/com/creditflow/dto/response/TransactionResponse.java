package com.creditflow.dto.response;

import com.creditflow.model.enums.PaymentMethod;
import com.creditflow.model.enums.TransactionStatus;
import com.creditflow.model.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {
    private Long id;
    private String reference;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private PaymentMethod paymentMethod;
    private String description;
    private LocalDateTime createdAt;
}
