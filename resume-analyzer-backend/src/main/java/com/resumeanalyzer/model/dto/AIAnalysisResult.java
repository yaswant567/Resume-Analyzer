package com.resumeanalyzer.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Maps the JSON object returned by the Claude resume-analysis prompt.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AIAnalysisResult(
        @JsonProperty("match_score") Integer matchScore,
        @JsonProperty("matched_keywords") List<String> matchedKeywords,
        @JsonProperty("missing_keywords") List<String> missingKeywords,
        @JsonProperty("strengths") List<String> strengths,
        @JsonProperty("improvements") List<String> improvements,
        @JsonProperty("summary") String summary
) {
}
