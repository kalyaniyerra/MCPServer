package com.cvs.pocs.mcpservers.conditions.model;

public class ChatResponse {
	 private String response;
	    private long timestamp;

	    // Constructors
	    public ChatResponse() {
	        this.timestamp = System.currentTimeMillis();
	    }
	    
	    public ChatResponse(String response) {
	        this.response = response;
	        this.timestamp = System.currentTimeMillis();
	    }

	    // Getters and Setters
	    public String getResponse() {
	        return response;
	    }

	    public void setResponse(String response) {
	        this.response = response;
	    }

	    public long getTimestamp() {
	        return timestamp;
	    }

	    public void setTimestamp(long timestamp) {
	        this.timestamp = timestamp;
	    }
}
