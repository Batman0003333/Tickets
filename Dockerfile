# 1. Use a lightweight Java 17 image (Change to 21 or 11 if your project uses a different version)
FROM eclipse-temurin:17-jre-alpine

# 2. Set the working directory inside the container
WORKDIR /app

# 3. Copy the compiled Spring Boot jar file into the container
# NOTE: If you use Gradle, change 'target/*.jar' to 'build/libs/*.jar'
COPY target/*.jar app.jar

# 4. Expose the port Spring Boot runs on (default is 8080)
EXPOSE 8080

# 5. The command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]