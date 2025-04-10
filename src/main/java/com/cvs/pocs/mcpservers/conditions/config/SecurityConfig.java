package com.cvs.pocs.mcpservers.conditions.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/api/v1/health").permitAll()
                    .anyRequest().authenticated()
            )
            .oauth2ResourceServer().jwt();
        
        http.sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            
        return http.build();
    }
}
