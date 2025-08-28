# Etapa 1: Build con Maven y Java 21
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -DskipTests
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Runtime con Java 21
FROM eclipse-temurin:21-jdk
WORKDIR /app
RUN mkdir -p /app/uploads
RUN chmod -R 777 /app/uploads
COPY --from=build /app/target/bdMetro-0.0.1-SNAPSHOT.jar java-app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "java-app.jar"]



#FROM eclipse-temurin:21-jdk
#WORKDIR /app
#RUN mkdir -p /app/uploads
#RUN chmod -R 777 /app/uploads
#COPY target/bdMetro-0.0.1-SNAPSHOT.jar java-app.jar
#EXPOSE 8080
#ENTRYPOINT ["java", "-jar", "java-app.jar"]