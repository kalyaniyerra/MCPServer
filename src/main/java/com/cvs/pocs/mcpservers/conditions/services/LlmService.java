package com.cvs.pocs.mcpservers.conditions.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LlmService {

    private static final Logger logger = LoggerFactory.getLogger(LlmService.class);

    @Value("${llm.api.url}")
    private String llmApiUrl;

    @Value("${llm.api.key}")
    private String llmApiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public LlmService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl(llmApiUrl).build();
        this.objectMapper = objectMapper;
    }

    public String processQuery(String clinicianQuery, String patientData) {
        try {
            // Construct a prompt that includes the patient data and the clinician's query
            String systemPrompt = "You are a clinical decision support assistant. " +
                "Your role is to help healthcare providers interpret patient data " +
                "and make informed decisions about potential conditions. " +
                "Use the patient data provided to answer the clinician's question accurately. " +
                "Make it clear when you're uncertain and avoid speculation. " +
                "Format your response for easy reading in a clinical setting.";

            String userPrompt = "CLINICIAN QUERY: " + clinicianQuery + "\n\n" +
                               "PATIENT DATA:\n" + patientData;

            // Create the request payload for the LLM API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4"); // Use appropriate model
            
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", userPrompt);
            
            requestBody.put("messages", List.of(systemMessage, userMessage));
            requestBody.put("temperature", 0.2); // Lower temperature for more predictable responses
            requestBody.put("max_tokens", 1000);

            // Send request to LLM API
            String responseJson = webClient.post()
                .headers(headers -> headers.set("Authorization", "Bearer " + llmApiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(String.class)
                .block();

            // Parse the response to extract the generated text
            // This is a simplified example; actual parsing would depend on the LLM API's response format
            Map<String, Object> responseMap = objectMapper.readValue(responseJson, Map.class);
            Map<String, Object> choicesMap = (Map<String, Object>) ((List) responseMap.get("choices")).get(0);
            Map<String, Object> messageMap = (Map<String, Object>) choicesMap.get("message");
            String generatedText = (String) messageMap.get("content");

            return generatedText.trim();

        } catch (Exception e) {
            logger.error("Error processing query with LLM", e);
            return "I'm sorry, but I couldn't process your question at this time. Please try again later.";
        }
    }
}