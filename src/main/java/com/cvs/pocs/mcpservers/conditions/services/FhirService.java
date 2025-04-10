package com.cvs.pocs.mcpservers.conditions.services;

//import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FhirService {

    private static final Logger logger = LoggerFactory.getLogger(FhirService.class);

    @Value("${fhir.server.url}")
    private String fhirServerUrl;

    @Autowired
    private WebClient.Builder webClientBuilder;

    private WebClient webClient;

    @Autowired
    public void initWebClient() {
        this.webClient = webClientBuilder.baseUrl(fhirServerUrl).build();
    }

    public String getRelevantPatientData(String patientId, String query) {
        StringBuilder patientData = new StringBuilder();
        
        // Add basic patient info
        patientData.append(getPatientDemographics(patientId));
        
        // Analyze the query to determine what data is needed
        if (containsKeyword(query, "lab", "laboratory", "test", "result")) {
            int months = extractTimeframe(query, 3); // Default to 3 months if not specified
            patientData.append(getLabResults(patientId, months));
        }
        
        if (containsKeyword(query, "condition", "diagnosis", "problem")) {
            patientData.append(getConditions(patientId));
        }
        
        if (containsKeyword(query, "medication", "med", "drug", "prescription")) {
            patientData.append(getMedications(patientId));
        }
        
        if (containsKeyword(query, "allergy", "allergies", "allergic")) {
            patientData.append(getAllergies(patientId));
        }
        
        if (containsKeyword(query, "vital", "vitals", "sign", "signs")) {
            int months = extractTimeframe(query, 1); // Default to 1 month if not specified
            patientData.append(getVitalSigns(patientId, months));
        }
        
        return patientData.toString();
    }

    private String getPatientDemographics(String patientId) {
        try {
            String patientJson = webClient.get()
                .uri("/Patient/{id}", patientId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
            
            // In a real implementation, parse the JSON and extract relevant fields
            return "PATIENT DEMOGRAPHICS:\n" + 
                   "ID: " + patientId + "\n" +
                   "(Demographics would be parsed from FHIR Patient resource)\n\n";
                   
        } catch (Exception e) {
            logger.error("Error retrieving patient demographics", e);
            return "PATIENT DEMOGRAPHICS: Unable to retrieve\n\n";
        }
    }

    private String getLabResults(String patientId, int months) {
        LocalDate cutoffDate = LocalDate.now().minusMonths(months);
        String dateParam = cutoffDate.format(DateTimeFormatter.ISO_DATE);
        
        try {
            String observationsJson = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/Observation")
                    .queryParam("patient", patientId)
                    .queryParam("category", "laboratory")
                    .queryParam("date", "ge" + dateParam)
                    .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
                
            // In a real implementation, parse the JSON and format lab results
            return "LABORATORY RESULTS (LAST " + months + " MONTHS):\n" +
                   "(Lab results would be parsed from FHIR Observation resources)\n\n";
                   
        } catch (Exception e) {
            logger.error("Error retrieving lab results", e);
            return "LABORATORY RESULTS: Unable to retrieve\n\n";
        }
    }

    private String getConditions(String patientId) {
        try {
            String conditionsJson = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/Condition")
                    .queryParam("patient", patientId)
                    .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
                
            // In a real implementation, parse the JSON and format conditions
            return "CONDITIONS:\n" +
                   "(Conditions would be parsed from FHIR Condition resources)\n\n";
                   
        } catch (Exception e) {
            logger.error("Error retrieving conditions", e);
            return "CONDITIONS: Unable to retrieve\n\n";
        }
    }

    private String getMedications(String patientId) {
        try {
            String medicationsJson = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/MedicationRequest")
                    .queryParam("patient", patientId)
                    .queryParam("status", "active")
                    .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
                
            // In a real implementation, parse the JSON and format medications
            return "ACTIVE MEDICATIONS:\n" +
                   "(Medications would be parsed from FHIR MedicationRequest resources)\n\n";
                   
        } catch (Exception e) {
            logger.error("Error retrieving medications", e);
            return "MEDICATIONS: Unable to retrieve\n\n";
        }
    }

    private String getAllergies(String patientId) {
        try {
            String allergiesJson = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/AllergyIntolerance")
                    .queryParam("patient", patientId)
                    .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
                
            // In a real implementation, parse the JSON and format allergies
            return "ALLERGIES:\n" +
                   "(Allergies would be parsed from FHIR AllergyIntolerance resources)\n\n";
                   
        } catch (Exception e) {
            logger.error("Error retrieving allergies", e);
            return "ALLERGIES: Unable to retrieve\n\n";
        }
    }

    private String getVitalSigns(String patientId, int months) {
        LocalDate cutoffDate = LocalDate.now().minusMonths(months);
        String dateParam = cutoffDate.format(DateTimeFormatter.ISO_DATE);
        
        try {
            String vitalsJson = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/Observation")
                    .queryParam("patient", patientId)
                    .queryParam("category", "vital-signs")
                    .queryParam("date", "ge" + dateParam)
                    .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
                
            // In a real implementation, parse the JSON and format vital signs
            return "VITAL SIGNS (LAST " + months + " MONTHS):\n" +
                   "(Vital signs would be parsed from FHIR Observation resources)\n\n";
                   
        } catch (Exception e) {
            logger.error("Error retrieving vital signs", e);
            return "VITAL SIGNS: Unable to retrieve\n\n";
        }
    }

    private boolean containsKeyword(String query, String... keywords) {
        String lowercaseQuery = query.toLowerCase();
        for (String keyword : keywords) {
            if (lowercaseQuery.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private int extractTimeframe(String query, int defaultMonths) {
        // Look for patterns like "last 2 months", "past 3 weeks", etc.
        Pattern pattern = Pattern.compile("(last|past)\\s+(\\d+)\\s+(month|months|week|weeks|day|days)");
        Matcher matcher = pattern.matcher(query.toLowerCase());
        
        if (matcher.find()) {
            int amount = Integer.parseInt(matcher.group(2));
            String unit = matcher.group(3);
            
            if (unit.startsWith("week")) {
                return Math.max(1, amount / 4); // Convert weeks to approximate months
            } else if (unit.startsWith("day")) {
                return Math.max(1, amount / 30); // Convert days to approximate months
            } else {
                return amount; // Already in months
            }
        }
        
        return defaultMonths;
    }
}