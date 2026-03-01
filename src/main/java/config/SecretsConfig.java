package config;

// import org.springframework.boot.context.properties.ConfigurationProperties;
// import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Secrets and Configuration Management
 * 
 * This configuration class handles loading and validating sensitive information from
 * environment variables. By centralizing secrets management, we ensure:
 * - No hardcoded credentials in source code
 * - Fail-fast validation on startup (immediate feedback if secrets are missing)
 * - Centralized access point for all configuration
 * 
 * 12-Factor App Compliance: https://12factor.net/config
 * All configuration is loaded from environment variables, not from code or files.
 */
@Configuration
public class SecretsConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecretsConfig.class);

    /**
     * Bean to validate and expose Apple Calendar secrets
     * 
     * Apple Calendar integration requires three pieces of information:
     * 1. APPLE_USR: Apple ID email address
     * 2. APPLE_SPEC_PW: App-specific password (not the regular Apple password)
     * 3. APPLE_CALDAV_URL: CalDAV server endpoint URL
     * 
     * App-specific passwords are generated in Apple ID settings and are safer than
     * storing the main Apple account password.
     * 
     * @return AppleCalendarSecrets containing validated configuration
     * @throws IllegalArgumentException if any required secret is missing
     */
    @Bean
    public AppleCalendarSecrets appleCalendarSecrets() {
        logger.info("Loading Apple Calendar secrets from environment variables");
        
        String appleUsr = System.getenv("APPLE_USR");
        String appleSpecPw = System.getenv("APPLE_SPEC_PW");
        String appleCalDavUrl = System.getenv("APPLE_CALDAV_URL");

        if (appleUsr == null || appleUsr.isEmpty()) {
            throw new IllegalArgumentException("Missing required environment variable: APPLE_USR");
        }
        if (appleSpecPw == null || appleSpecPw.isEmpty()) {
            throw new IllegalArgumentException("Missing required environment variable: APPLE_SPEC_PW");
        }
        if (appleCalDavUrl == null || appleCalDavUrl.isEmpty()) {
            throw new IllegalArgumentException("Missing required environment variable: APPLE_CALDAV_URL");
        }

        logger.debug("Apple Calendar secrets validated successfully");
        return new AppleCalendarSecrets(appleUsr, appleSpecPw, appleCalDavUrl);
    }

    /**
     * Bean to validate and expose Google Webhook secrets
     * 
     * Google Calendar webhooks require subscription channel credentials to verify
     * that webhook requests are legitimate and come from Google.
     * 
     * @return GoogleWebhookSecrets containing validated configuration
     * @throws IllegalArgumentException if any required secret is missing
     */
    @Bean
    public GoogleWebhookSecrets googleWebhookSecrets() {
        logger.info("Loading Google Webhook secrets from environment variables");
        
        String channelId = System.getenv("WEBHOOK_GOOGLE_CHANNEL_ID");
        String channelToken = System.getenv("WEBHOOK_GOOGLE_CHANNEL_TOKEN");

        if (channelId == null || channelId.isEmpty()) {
            logger.warn("WEBHOOK_GOOGLE_CHANNEL_ID not set - webhook validation may be limited");
        }
        if (channelToken == null || channelToken.isEmpty()) {
            logger.warn("WEBHOOK_GOOGLE_CHANNEL_TOKEN not set - webhook validation may be limited");
        }

        logger.debug("Google Webhook secrets loaded");
        return new GoogleWebhookSecrets(channelId, channelToken);
    }

    /**
     * Inner class to hold Apple Calendar secrets
     */
    public static class AppleCalendarSecrets {
        public final String username;
        public final String appSpecificPassword;
        public final String calDavUrl;

        public AppleCalendarSecrets(String username, String appSpecificPassword, String calDavUrl) {
            this.username = username;
            this.appSpecificPassword = appSpecificPassword;
            this.calDavUrl = calDavUrl;
        }
    }

    /**
     * Inner class to hold Google Webhook secrets
     */
    public static class GoogleWebhookSecrets {
        public final String channelId;
        public final String channelToken;

        public GoogleWebhookSecrets(String channelId, String channelToken) {
            this.channelId = channelId;
            this.channelToken = channelToken;
        }
    }
}
