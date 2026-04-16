# ==========================================
# Stage 1: Build the application (Compile)
# ==========================================
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Tell Docker to look inside the 'demo' folder on GitHub
COPY demo/pom.xml .
COPY demo/src ./src

# Compile the Java code into a .jar file
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Run the application (Deploy)
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the compiled .jar file
COPY --from=builder /app/target/*.jar app.jar

# Expose the port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]