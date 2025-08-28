# Etapa 1: Build con Maven y Java 23
FROM maven:3.9.6-eclipse-temurin-23 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Runtime con Java 23
FROM eclipse-temurin:23-jdk
WORKDIR /app

# Crear directorio para uploads
RUN mkdir -p /data/uploads

# Copiar JAR
COPY --from=build /app/target/*.jar app.jar

# Variables de entorno
ENV SPRING_PROFILES_ACTIVE=production

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=production", "-jar", "app.jar"]