package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * Past de firewall aan zodat Google Calendar webhook URLs met "//" niet
     * geblokkeerd worden. Google stuurt soms URLs mee in headers die dubbele
     * slashes bevatten (bijv. in X-Goog-Resource-URI).
     */
    @Bean
    public HttpFirewall allowDoubleSlashFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedDoubleSlash(true);
        return firewall;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring HTTP security filter chain");

        http
            .authorizeRequests()
                .antMatchers("/api/health/**").permitAll()
                .antMatchers("/agendasyc/api/health/**").permitAll()
                // Webhook endpoint: Google POST requests komen hier binnen
                .antMatchers("/api/webhooks/**").permitAll()
                .antMatchers("/agendasyc/api/webhooks/**").permitAll()
                .anyRequest().authenticated()
                .and()
            // CSRF uitschakelen voor webhook endpoint — Google stuurt geen CSRF token
            // De webhook wordt gevalideerd via de X-Goog-Channel-Token header
            .csrf()
                .ignoringAntMatchers("/api/webhooks/**")
                .and()
            .csrf()
                .csrfTokenRepository(new org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository());

        logger.debug("HTTP security filter chain configured successfully");
        return http.build();
    }
}