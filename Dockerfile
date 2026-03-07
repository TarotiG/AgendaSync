# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Copy Gradle wrapper and dependency files first for layer caching
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle

# Download dependencies (cached layer as long as build.gradle doesn't change)
RUN gradle dependencies --no-daemon || true

# Copy source code and build
COPY src ./src
RUN gradle bootJar -x test --no-daemon

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Copy credentials (needed for Google OAuth)
COPY agendasync-474013-82a844cdf0f6.json /etc/secrets/agendasync-474013-82a844cdf0f6.json

# Expose Spring Boot default port
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
