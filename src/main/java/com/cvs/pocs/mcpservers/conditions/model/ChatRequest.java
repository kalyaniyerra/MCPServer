package com.cvs.pocs.mcpservers.conditions.model;

public class ChatRequest {
	private String patientId;
    private String query;
    private String userId;  // ID of the clinician making the request

    // Constructors
    public ChatRequest() {}
    
    public ChatRequest(String patientId, String query, String userId) {
        this.patientId = patientId;
        this.query = query;
        this.userId = userId;
    }

    // Getters and Setters
    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
