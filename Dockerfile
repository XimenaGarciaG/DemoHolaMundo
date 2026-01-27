# Usa Java 17
FROM eclipse-temurin:17-jdk-alpine

# Directorio de trabajo
WORKDIR /app

# Copia el JAR generado
COPY target/*.jar app.jar

# Puerto que usa Spring Boot
EXPOSE 8088

# Cambia el Dockerfile a:
ENTRYPOINT ["java", "-jar", "/app/target/*.jar"]