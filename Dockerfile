# Etapa 1: Build con Maven y Java 21




#FROM eclipse-temurin:21-jdk
#WORKDIR /app
#RUN mkdir -p /app/uploads
#RUN chmod -R 777 /app/uploads
#COPY target/bdMetro-0.0.1-SNAPSHOT.jar java-app.jar
#EXPOSE 8080
#ENTRYPOINT ["java", "-jar", "java-app.jar"]

# Usar imagen base de Eclipse Temurin Java 21
FROM eclipse-temurin:21-jdk

# Crear directorio para uploads
RUN mkdir -p /app/uploads
RUN chmod -R 777 /app/uploads

# Copiar el JAR compilado localmente
COPY target/bdMetro-0.0.1-SNAPSHOT.jar java-app.jar

# Exponer el puerto 8080
EXPOSE 8080

# Configurar el comando de entrada para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "java-app.jar"]