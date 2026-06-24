package com.resumeanalyzer.service.ai;

import com.resumeanalyzer.model.dto.AIAnalysisResult;

/**
 * A pluggable AI backend that scores a resume against a job description.
 * The active implementation is selected via the {@code app.ai.provider} property.
 */
public interface AIProvider {

    AIAnalysisResult analyze(String resumeText, String jobDescription);
}
