package com.creditflow.service;

import com.creditflow.dto.response.KycDocumentResponse;
import com.creditflow.exception.BadRequestException;
import com.creditflow.exception.ResourceNotFoundException;
import com.creditflow.model.KycDocument;
import com.creditflow.model.User;
import com.creditflow.model.enums.KycStatus;
import com.creditflow.repository.KycDocumentRepository;
import com.creditflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KycService {

    private final KycDocumentRepository kycDocumentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Value("${creditflow.upload.dir}")
    private String uploadDir;

    @Transactional
    public KycDocument submitDocuments(Long userId,
                                       MultipartFile nationalId,
                                       MultipartFile policeClearance,
                                       MultipartFile selfie) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (user.getKycStatus() == KycStatus.APPROVED) {
            throw new BadRequestException("KYC is already approved");
        }

        KycDocument doc = kycDocumentRepository.findByUserId(userId)
                .orElse(KycDocument.builder().user(user).build());

        if (nationalId != null && !nationalId.isEmpty()) {
            doc.setNationalIdPath(saveFile(userId, "national_id", nationalId));
        }
        if (policeClearance != null && !policeClearance.isEmpty()) {
            doc.setPoliceClearancePath(saveFile(userId, "police_clearance", policeClearance));
        }
        if (selfie != null && !selfie.isEmpty()) {
            doc.setSelfiePath(saveFile(userId, "selfie", selfie));
        }

        doc.setStatus(KycStatus.PENDING_REVIEW);
        user.setKycStatus(KycStatus.PENDING_REVIEW);
        userRepository.save(user);
        doc = kycDocumentRepository.save(doc);

        notificationService.notify(user, "KYC Documents Submitted",
                "Your KYC documents have been submitted and are under review. We will notify you once reviewed.");

        return doc;
    }

    @Transactional
    public KycDocument reviewKyc(Long kycId, KycStatus decision, String rejectionReason, Long adminId) {
        KycDocument doc = kycDocumentRepository.findById(kycId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC document", kycId));

        if (decision != KycStatus.APPROVED && decision != KycStatus.REJECTED) {
            throw new BadRequestException("Decision must be APPROVED or REJECTED");
        }

        doc.setStatus(decision);
        doc.setReviewedBy(adminId);
        doc.setRejectionReason(rejectionReason);
        doc.getUser().setKycStatus(decision);
        userRepository.save(doc.getUser());
        doc = kycDocumentRepository.save(doc);

        String title = decision == KycStatus.APPROVED ? "KYC Approved" : "KYC Rejected";
        String message = decision == KycStatus.APPROVED
                ? "Your identity has been verified. You can now apply for loans."
                : "Your KYC was rejected. Reason: " + rejectionReason + ". Please resubmit.";

        notificationService.notify(doc.getUser(), title, message);
        return doc;
    }

    @Transactional(readOnly = true)
    public List<KycDocumentResponse> getPendingKycDocuments() {
        return kycDocumentRepository.findByStatus(KycStatus.PENDING_REVIEW)
                .stream().map(this::toResponse).toList();
    }

    private KycDocumentResponse toResponse(KycDocument doc) {
        KycDocumentResponse.KycUserSummary userSummary = null;
        if (doc.getUser() != null) {
            User u = doc.getUser();
            userSummary = KycDocumentResponse.KycUserSummary.builder()
                    .id(u.getId())
                    .firstName(u.getFirstName())
                    .lastName(u.getLastName())
                    .email(u.getEmail())
                    .phoneNumber(u.getPhoneNumber())
                    .role(u.getRole() != null ? u.getRole().name() : null)
                    .kycStatus(u.getKycStatus() != null ? u.getKycStatus().name() : null)
                    .build();
        }
        return KycDocumentResponse.builder()
                .id(doc.getId())
                .user(userSummary)
                .status(doc.getStatus() != null ? doc.getStatus().name() : null)
                .nationalIdPath(doc.getNationalIdPath())
                .policeClearancePath(doc.getPoliceClearancePath())
                .selfiePath(doc.getSelfiePath())
                .submittedAt(doc.getSubmittedAt())
                .rejectionReason(doc.getRejectionReason())
                .build();
    }

    @Transactional(readOnly = true)
    public KycDocument getUserKyc(Long userId) {
        return kycDocumentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC not found for user: " + userId));
    }

    private String saveFile(Long userId, String type, MultipartFile file) throws IOException {
        Path dir = Paths.get(uploadDir, String.valueOf(userId));
        Files.createDirectories(dir);
        String extension = getExtension(file.getOriginalFilename());
        String filename = type + "_" + UUID.randomUUID() + extension;
        Path target = dir.resolve(filename);
        Files.write(target, file.getBytes());
        return target.toString();
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.'));
    }
}
