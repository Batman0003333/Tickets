# ==========================================
# Stage 1: Build the application (Compile)
# ==========================================
# Changed from temurin-17 to temurin-21
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY demo/pom.xml .
COPY demo/src ./src

RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Run the application (Deploy)
# ==========================================
# Changed from temurin-17 to temurin-21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]