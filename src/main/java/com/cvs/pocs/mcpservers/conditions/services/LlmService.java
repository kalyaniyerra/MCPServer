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
            
            // Generate a realistic mock response based on the query type
            String queryLower = clinicianQuery.toLowerCase();
            
            if (queryLower.contains("lab") || queryLower.contains("test") || queryLower.contains("result")) {
                return "Based on the patient's laboratory results:\n" +
                       "- Complete Blood Count (CBC) shows normal ranges\n" +
                       "- Basic Metabolic Panel (BMP) indicates stable electrolyte levels\n" +
                       "- Lipid panel shows slightly elevated LDL cholesterol\n" +
                       "Note: This is a mock response due to LLM service unavailability.";
            } else if (queryLower.contains("medication") || queryLower.contains("drug") || queryLower.contains("prescription")) {
                return "Current medication status:\n" +
                       "- Metformin 500mg twice daily for Type 2 Diabetes\n" +
                       "- Lisinopril 10mg daily for hypertension\n" +
                       "- Aspirin 81mg daily for cardiovascular prevention\n" +
                       "Note: This is a mock response due to LLM service unavailability.";
            } else if (queryLower.contains("condition") || queryLower.contains("diagnosis") || queryLower.contains("problem")) {
                return "Active conditions:\n" +
                       "- Type 2 Diabetes (well-controlled)\n" +
                       "- Essential Hypertension\n" +
                       "- Hyperlipidemia\n" +
                       "Note: This is a mock response due to LLM service unavailability.";
            } else if (queryLower.contains("allergy") || queryLower.contains("allergic")) {
                return "Known allergies:\n" +
                       "- Penicillin (moderate reaction)\n" +
                       "- Sulfa drugs (mild reaction)\n" +
                       "Note: This is a mock response due to LLM service unavailability.";
            } else if (queryLower.contains("diagnose") || queryLower.contains("add diagnosis") || queryLower.contains("suggest diagnosis")) {
                // Randomly choose between supporting or rejecting the diagnosis
                boolean supportDiagnosis = Math.random() > 0.5;
                
                if (supportDiagnosis) {
                    return "Based on the patient's lab values and current medications, I support adding the following diagnosis:\n" +
                           "- The HbA1c of 7.2% and fasting glucose of 126 mg/dL support a diagnosis of Type 2 Diabetes\n" +
                           "- The blood pressure readings consistently above 140/90 mmHg support Essential Hypertension\n" +
                           "- The LDL cholesterol of 130 mg/dL and total cholesterol of 220 mg/dL support Hyperlipidemia\n" +
                           "These diagnoses are consistent with the patient's current medication regimen of Metformin, Lisinopril, and statin therapy.\n" +
                           "Note: This is a mock response due to LLM service unavailability.";
                } else {
                    return "Based on the patient's lab values and current medications, I cannot support adding the suggested diagnosis:\n" +
                           "- The lab values do not meet diagnostic criteria for the proposed condition\n" +
                           "- The patient's current medications and lab results suggest better control of existing conditions\n" +
                           "- Additional testing would be needed to confirm the diagnosis\n" +
                           "Recommendation: Continue current treatment plan and monitor for changes in condition.\n" +
                           "Note: This is a mock response due to LLM service unavailability.";
                }
            } else if (queryLower.contains("reject") || queryLower.contains("remove diagnosis") || queryLower.contains("discontinue diagnosis")) {
                return "Based on the patient's lab values and current medications, I support rejecting/removing the following diagnosis:\n" +
                       "- The most recent HbA1c of 5.7% and normal fasting glucose values no longer support Type 2 Diabetes\n" +
                       "- Blood pressure readings consistently below 130/80 mmHg suggest resolution of Essential Hypertension\n" +
                       "- Normal lipid panel results suggest resolution of Hyperlipidemia\n" +
                       "Recommendation: Consider discontinuing related medications and updating the patient's problem list.\n" +
                       "Note: This is a mock response due to LLM service unavailability.";
            } else {
                return "I apologize, but I'm currently unable to process your query due to technical difficulties. " +
                       "Please try again later or contact technical support if the issue persists.";
            }
        }
    }
}