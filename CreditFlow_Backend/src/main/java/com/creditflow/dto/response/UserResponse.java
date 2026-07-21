package com.creditflow.dto.response;

import com.creditflow.model.enums.KycStatus;
import com.creditflow.model.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private UserRole role;
    private KycStatus kycStatus;
    private BigDecimal walletBalance;
    private boolean active;
    private boolean deleted;
    private LocalDateTime createdAt;
}
