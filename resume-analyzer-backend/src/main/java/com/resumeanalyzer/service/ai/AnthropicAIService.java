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

@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "anthropic", matchIfMissing = true)
public class AnthropicAIService extends AbstractAIProvider {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public AnthropicAIService(
            ObjectMapper objectMapper,
            ClientHttpRequestFactory aiRequestFactory,
            @Value("${app.ai.anthropic.base-url}") String baseUrl,
            @Value("${app.ai.anthropic.api-key}") String apiKey,
            @Value("${app.ai.anthropic.model}") String model
    ) {
        super(objectMapper);
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(aiRequestFactory).build();
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public AIAnalysisResult analyze(String resumeText, String jobDescription) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AIServiceException("AI service is not configured (missing Anthropic API key)");
        }

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", 2048,
                "system", SYSTEM_PROMPT,
                "messages", List.of(
                        Map.of("role", "user", "content", buildUserPrompt(resumeText, jobDescription))
                )
        );

        AnthropicResponse response;
        try {
            response = restClient.post()
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("content-type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(AnthropicResponse.class);
        } catch (RestClientException ex) {
            throw new AIServiceException("Failed to call Claude API", ex);
        }

        if (response == null || response.content() == null || response.content().isEmpty()) {
            throw new AIServiceException("Claude API returned an empty response");
        }

        return parseResult(response.content().get(0).text());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AnthropicResponse(List<ContentBlock> content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ContentBlock(String type, String text) {
    }
}
