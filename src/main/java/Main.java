import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for AgendaSync Spring Boot Application.
 * 
 * This application synchronizes events between Apple Calendar and Google Calendar:
 * - Polls Apple Calendar every 15 minutes for new/updated events
 * - Receives Google Calendar webhook notifications for real-time updates
 * - Syncs events bidirectionally to maintain calendar parity
 * 
 * Features:
 * - Spring Boot Auto-configuration for dependency management
 * - Scheduled polling for Apple Calendar (CalDAV)
 * - REST endpoint for Google Calendar webhooks
 * - SLF4J logging with Logback configuration
 * - Environment-based secrets management
 * - Stateful sync tracking to minimize API calls
 */
@SpringBootApplication(scanBasePackages = {
    "sync",
    "scheduler",
    "calendar",
    "rest",
    "config",
    "syncengine"
})
@EnableScheduling
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Starting AgendaSync Application...");
        SpringApplication.run(Main.class, args);
        logger.info("AgendaSync Application started successfully");
    }
}