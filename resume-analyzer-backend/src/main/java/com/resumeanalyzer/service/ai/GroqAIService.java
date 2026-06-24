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
 * Groq provider. OpenAI-compatible chat completions API, free tier with
 * generous rate limits for models like llama-3.3-70b-versatile.
 */
@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "groq")
public class GroqAIService extends AbstractAIProvider {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GroqAIService(
            ObjectMapper objectMapper,
            ClientHttpRequestFactory aiRequestFactory,
            @Value("${app.ai.groq.base-url}") String baseUrl,
            @Value("${app.ai.groq.api-key}") String apiKey,
            @Value("${app.ai.groq.model}") String model
    ) {
        super(objectMapper);
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(aiRequestFactory).build();
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public AIAnalysisResult analyze(String resumeText, String jobDescription) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AIServiceException("AI service is not configured (missing Groq API key)");
        }

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "temperature", 0.3,
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", buildUserPrompt(resumeText, jobDescription))
                )
        );

        GroqResponse response;
        try {
            response = restClient.post()
                    .header("Authorization", "Bearer " + apiKey)
                    .header("content-type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(GroqResponse.class);
        } catch (RestClientException ex) {
            throw new AIServiceException("Failed to call Groq API", ex);
        }

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new AIServiceException("Groq API returned an empty response");
        }

        return parseResult(response.choices().get(0).message().content());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GroqResponse(List<Choice> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Choice(Message message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Message(String role, String content) {
    }
}
