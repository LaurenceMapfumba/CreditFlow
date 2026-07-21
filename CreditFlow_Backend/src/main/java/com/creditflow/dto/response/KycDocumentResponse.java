package com.creditflow.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class KycDocumentResponse {
    private Long id;
    private KycUserSummary user;
    private String status;
    private String nationalIdPath;
    private String policeClearancePath;
    private String selfiePath;
    private LocalDateTime submittedAt;
    private String rejectionReason;

    @Data
    @Builder
    public static class KycUserSummary {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String role;
        private String kycStatus;
    }
}
