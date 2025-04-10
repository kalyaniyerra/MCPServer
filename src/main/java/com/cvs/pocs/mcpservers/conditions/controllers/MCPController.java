package com.cvs.pocs.mcpservers.conditions.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cvs.pocs.mcpservers.conditions.model.ChatRequest;
import com.cvs.pocs.mcpservers.conditions.model.ChatResponse;
import com.cvs.pocs.mcpservers.conditions.services.FhirService;
import com.cvs.pocs.mcpservers.conditions.services.LlmService;

@RestController
@RequestMapping("/api/v1")
public class MCPController {

    private static final Logger logger = LoggerFactory.getLogger(MCPController.class);

    @Autowired
    private FhirService fhirService;

    @Autowired
    private LlmService llmService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> processChat(@RequestBody ChatRequest chatRequest) {
        logger.info("Received chat request for patient ID: {}", chatRequest.getPatientId());
		return null;
        
     /*   try {
            // Step 1: Extract relevant patient data based on the query
            String patientData = fhirService.getRelevantPatientData(
                chatRequest.getPatientId(), 
                chatRequest.getQuery()
            );
            
            // Step 2: Process the query with the LLM using patient data
            String llmResponse = llmService.processQuery(chatRequest.getQuery(), patientData);
            
            // Step 3: Return the response
            ChatResponse response = new ChatResponse(llmResponse);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing chat request", e);
            return ResponseEntity.internalServerError().body(
                new ChatResponse("Sorry, I encountered an error while processing your request.")
            );
        }*/
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("MCP Server is running");
    }
}