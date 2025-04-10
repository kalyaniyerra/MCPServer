package com.cvs.pocs.mcpservers.conditions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
//mcp server conditions
@SpringBootApplication
public class ConditionsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConditionsApplication.class, args);
	}


    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
