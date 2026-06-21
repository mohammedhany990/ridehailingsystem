# ─────────────────────────────────────────────────────────────────────────────
# Stage 1 — Build
# Uses the full Maven + JDK image to compile and package the JAR
# ─────────────────────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy the parent pom first (for dependency caching)
# If building a single service, just copy its pom.xml
COPY pom.xml .

# Download all dependencies before copying source
# This layer is cached as long as pom.xml doesn't change
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the JAR, skip tests (tests run in CI separately)
RUN mvn package -DskipTests -B

# ─────────────────────────────────────────────────────────────────────────────
# Stage 2 — Run
# Uses a minimal JRE Alpine image — much smaller than the build image
# Build image: ~700MB   →   Runtime image: ~200MB
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy only the compiled JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Give ownership to non-root user
RUN chown appuser:appgroup app.jar

USER appuser

# Expose the port (overridden per service in docker-compose)
EXPOSE 8080

# JVM tuning flags:
#   -XX:+UseContainerSupport        → respects Docker CPU/memory limits
#   -XX:MaxRAMPercentage=75.0       → use 75% of container RAM for heap
#   -Djava.security.egd=...         → faster startup (avoids entropy issues)
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
