package com.resumeanalyzer.service.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeanalyzer.exception.CustomExceptions.AIServiceException;
import com.resumeanalyzer.model.dto.AIAnalysisResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/**
 * Google Gemini (Generative Language API) provider. Uses the free-tier
 * "generateContent" endpoint, e.g. gemini-1.5-flash or gemini-2.0-flash.
 */
@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "gemini")
public class GeminiAIService extends AbstractAIProvider {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiAIService(
            ObjectMapper objectMapper,
            ClientHttpRequestFactory aiRequestFactory,
            @Value("${app.ai.gemini.base-url}") String baseUrl,
            @Value("${app.ai.gemini.api-key}") String apiKey,
            @Value("${app.ai.gemini.model}") String model
    ) {
        super(objectMapper);
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(aiRequestFactory).build();
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public AIAnalysisResult analyze(String resumeText, String jobDescription) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AIServiceException("AI service is not configured (missing Gemini API key)");
        }

        Map<String, Object> requestBody = Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", SYSTEM_PROMPT))),
                "contents", List.of(
                        Map.of("role", "user", "parts", List.of(Map.of("text", buildUserPrompt(resumeText, jobDescription))))
                )
        );

        GeminiResponse response;
        try {
            response = restClient.post()
                    .uri("/{model}:generateContent?key={apiKey}", model, apiKey)
                    .header("content-type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(GeminiResponse.class);
        } catch (RestClientException ex) {
            throw new AIServiceException("Failed to call Gemini API", ex);
        }

        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            throw new AIServiceException("Gemini API returned an empty response");
        }

        Content content = response.candidates().get(0).content();
        if (content == null || content.parts() == null || content.parts().isEmpty()) {
            throw new AIServiceException("Gemini API returned an empty response");
        }

        return parseResult(content.parts().get(0).text());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeminiResponse(List<Candidate> candidates) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Candidate(Content content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Content(List<Part> parts) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Part(String text) {
    }
}
