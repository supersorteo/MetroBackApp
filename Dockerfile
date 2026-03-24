FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -DskipTests clean package

FROM eclipse-temurin:21-jdk

WORKDIR /app

RUN mkdir -p /app/uploads
RUN chmod -R 777 /app/uploads

COPY --from=build /app/target/bdMetro-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_PROFILES_ACTIVE=production

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
