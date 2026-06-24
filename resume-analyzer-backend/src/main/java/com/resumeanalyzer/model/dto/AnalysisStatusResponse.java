package com.resumeanalyzer.model.dto;

import com.resumeanalyzer.model.entity.AnalysisStatus;

import java.util.UUID;

public record AnalysisStatusResponse(
        UUID id,
        AnalysisStatus status,
        String errorMessage
) {
}
