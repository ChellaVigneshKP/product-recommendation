FROM openjdk:21-jdk-slim
LABEL authors="Chella Vignesh K P"

WORKDIR /app

# Copy the jar file into the container at /app
COPY target/service-discovery-*.jar service-discovery.jar

# Make port 8761 available to the world outside this container
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "/app/service-discovery.jar"]