package com.resumeanalyzer.model.dto;

import com.resumeanalyzer.model.entity.Analysis;
import com.resumeanalyzer.model.entity.AnalysisStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AnalysisResponse(
        UUID id,
        Integer matchScore,
        List<String> matchedKeywords,
        List<String> missingKeywords,
        List<String> strengths,
        List<String> improvements,
        String summary,
        AnalysisStatus status,
        String errorMessage,
        Instant createdAt
) {
    public static AnalysisResponse from(Analysis analysis) {
        return new AnalysisResponse(
                analysis.getId(),
                analysis.getMatchScore(),
                analysis.getMatchedKeywords(),
                analysis.getMissingKeywords(),
                analysis.getStrengths(),
                analysis.getImprovements(),
                analysis.getSummary(),
                analysis.getStatus(),
                analysis.getErrorMessage(),
                analysis.getCreatedAt()
        );
    }
}
