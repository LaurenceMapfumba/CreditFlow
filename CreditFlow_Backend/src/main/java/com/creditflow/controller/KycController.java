package com.creditflow.controller;

import com.creditflow.dto.response.ApiResponse;
import com.creditflow.model.KycDocument;
import com.creditflow.model.enums.KycStatus;
import com.creditflow.security.UserPrincipal;
import com.creditflow.service.KycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
@Tag(name = "KYC", description = "Know Your Customer verification endpoints")
public class KycController {

    private final KycService kycService;

    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Submit KYC documents (National ID, Police Clearance, Selfie)")
    public ResponseEntity<ApiResponse<Void>> submit(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart(value = "nationalId", required = false) MultipartFile nationalId,
            @RequestPart(value = "policeClearance", required = false) MultipartFile policeClearance,
            @RequestPart(value = "selfie", required = false) MultipartFile selfie) throws IOException {
        kycService.submitDocuments(principal.getId(), nationalId, policeClearance, selfie);
        return ResponseEntity.ok(ApiResponse.success("KYC documents submitted. Awaiting review."));
    }

    @GetMapping("/status")
    @Operation(summary = "Get current user KYC status")
    public ResponseEntity<ApiResponse<KycDocument>> getStatus(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(kycService.getUserKyc(principal.getId())));
    }
}
