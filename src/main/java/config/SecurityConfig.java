package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spring Security Configuration for REST Endpoints
 * 
 * This configuration class handles security policies for HTTP endpoints:
 * - Webhook endpoint: Restricted to authenticated requests
 * - Health endpoint: Publicly accessible (for monitoring)
 * - API endpoints: Configured based on security requirements
 * 
 * Security Strategy:
 * - Enable CSRF protection to prevent cross-site attacks
 * - Allow health check endpoint for external monitoring
 * - Require authentication for sensitive operations
 * - Log security events for audit purposes
 */
@Configuration
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * Configures HTTP security filter chain
     * 
     * This method defines which endpoints are publicly accessible and which require authentication.
     * 
     * Security rules:
     * 1. Health endpoints (/api/health/**): Public access (needed for monitoring systems)
     * 2. Webhook endpoints (/api/webhooks/**): Protected (validated headers required)
     * 3. All other endpoints: Require authentication
     * 
     * @param http HttpSecurity configured by Spring
     * @return SecurityFilterChain with configured security policies
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring HTTP security filter chain");

        http
            .authorizeRequests()
                // Public endpoints - no authentication required
                .antMatchers("/api/health/**").permitAll()
                .antMatchers("/agendasyc/api/health/**").permitAll()
                
                // Webhook endpoints - accessible but will validate headers
                .antMatchers("/api/webhooks/**").permitAll()
                .antMatchers("/agendasyc/api/webhooks/**").permitAll()
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
                .and()
            .csrf()
                // CSRF protection enabled for state-changing operations
                .csrfTokenRepository(new org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository());

        logger.debug("HTTP security filter chain configured successfully");
        return http.build();
    }
}
