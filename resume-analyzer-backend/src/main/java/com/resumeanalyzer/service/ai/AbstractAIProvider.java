package com.resumeanalyzer.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeanalyzer.exception.CustomExceptions.AIServiceException;
import com.resumeanalyzer.model.dto.AIAnalysisResult;

/**
 * Shared prompt-building and response-parsing logic for all {@link AIProvider} implementations.
 */
public abstract class AbstractAIProvider implements AIProvider {

    protected static final String SYSTEM_PROMPT =
            "You are an expert ATS resume screener and career coach.";

    private final ObjectMapper objectMapper;

    protected AbstractAIProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    protected String buildUserPrompt(String resumeText, String jobDescription) {
        return """
                Analyze the following resume against the job description.

                RESUME:
                %s

                JOB DESCRIPTION:
                %s

                Return ONLY a valid JSON object with no extra text:
                {
                  "match_score": (integer 0-100),
                  "matched_keywords": ["keyword1", "keyword2"],
                  "missing_keywords": ["keyword1", "keyword2"],
                  "strengths": ["strength1", "strength2"],
                  "improvements": ["improvement1", "improvement2"],
                  "summary": "2-3 line overall feedback"
                }
                """.formatted(resumeText, jobDescription);
    }

    /**
     * Models sometimes wrap JSON in markdown code fences despite instructions.
     * Strip any surrounding fences/whitespace and isolate the {...} block, then parse it.
     */
    protected AIAnalysisResult parseResult(String rawText) {
        if (rawText == null) {
            throw new AIServiceException("AI provider returned an empty response");
        }

        String trimmed = rawText.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');

        if (start == -1 || end == -1 || end < start) {
            throw new AIServiceException("AI provider response did not contain a JSON object");
        }

        String json = trimmed.substring(start, end + 1);

        try {
            return objectMapper.readValue(json, AIAnalysisResult.class);
        } catch (Exception ex) {
            throw new AIServiceException("Failed to parse AI provider response as JSON", ex);
        }
    }
}
