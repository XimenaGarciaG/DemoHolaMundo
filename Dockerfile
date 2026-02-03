# Dockerfile simple para Spring Boot
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copia el código
COPY . .

# Da permisos a mvnw
RUN chmod +x mvnw

# Construye la aplicación
RUN ./mvnw clean package -DskipTests

# Puerto
EXPOSE 8080

# Ejecuta
CMD ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]