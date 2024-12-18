FROM openjdk:21-jdk-slim
LABEL authors="Chella Vignesh K P"
WORKDIR /app

# Copy the jar file from the target directory (build output) into the container
COPY target/products-*.jar products.jar

# Expose the port on which the app will run (Cloud Run expects port 8080)
EXPOSE 8080

# Run the jar file when the container starts
ENTRYPOINT ["java", "-jar", "/app/products.jar"]