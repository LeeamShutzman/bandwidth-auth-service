# Use the standard JRE 21 image (which supports both x86 and ARM64)
FROM eclipse-temurin:21-jre

WORKDIR /app

# Point exactly to your jar
COPY target/auth-service-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]