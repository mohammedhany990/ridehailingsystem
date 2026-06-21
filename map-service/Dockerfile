# ── Build stage ───────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:resolve -B -q 2>/dev/null || true
COPY src src
RUN ./mvnw package -DskipTests -B -q

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup --system app && adduser --system --ingroup app app
COPY --from=build /app/target/*.jar app.jar
USER app
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
