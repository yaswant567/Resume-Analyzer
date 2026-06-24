package com.resumeanalyzer.repository;

import com.resumeanalyzer.model.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnalysisRepository extends JpaRepository<Analysis, UUID> {

    List<Analysis> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Analysis> findByIdAndUserId(UUID id, UUID userId);
}
