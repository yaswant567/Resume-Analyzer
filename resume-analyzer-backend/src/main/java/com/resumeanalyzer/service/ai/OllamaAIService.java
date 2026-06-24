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

import java.util.Map;

/**
 * Local Ollama provider. Talks to a locally-running Ollama instance,
 * no API key required.
 */
@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "ollama")
public class OllamaAIService extends AbstractAIProvider {

    private final RestClient restClient;
    private final String model;

    public OllamaAIService(
            ObjectMapper objectMapper,
            ClientHttpRequestFactory aiRequestFactory,
            @Value("${app.ai.ollama.base-url}") String baseUrl,
            @Value("${app.ai.ollama.model}") String model
    ) {
        super(objectMapper);
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(aiRequestFactory).build();
        this.model = model;
    }

    @Override
    public AIAnalysisResult analyze(String resumeText, String jobDescription) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "system", SYSTEM_PROMPT,
                "prompt", buildUserPrompt(resumeText, jobDescription),
                "stream", false
        );

        OllamaResponse response;
        try {
            response = restClient.post()
                    .uri("/api/generate")
                    .header("content-type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(OllamaResponse.class);
        } catch (RestClientException ex) {
            throw new AIServiceException("Failed to call Ollama API. Is Ollama running and is the model pulled?", ex);
        }

        if (response == null || response.response() == null || response.response().isBlank()) {
            throw new AIServiceException("Ollama API returned an empty response");
        }

        return parseResult(response.response());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OllamaResponse(String response) {
    }
}
