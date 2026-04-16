# ==========================================
# Stage 1: Build the application (Compile)
# ==========================================
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy your pom.xml and source code into the Docker environment
COPY pom.xml .
COPY src ./src

# Compile the Java code into a .jar file (skipping tests to speed up deployment)
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Run the application (Deploy)
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy ONLY the finished .jar file from the 'builder' stage above
COPY --from=builder /app/target/*.jar app.jar

# Expose the port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]