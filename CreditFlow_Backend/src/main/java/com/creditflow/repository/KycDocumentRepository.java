package com.creditflow.repository;

import com.creditflow.model.KycDocument;
import com.creditflow.model.enums.KycStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {
    Optional<KycDocument> findByUserId(Long userId);
    List<KycDocument> findByStatus(KycStatus status);
}
