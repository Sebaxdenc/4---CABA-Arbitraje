FROM eclipse-temurin:24-jre-alpine

WORKDIR /app

#Copiar el .jar de la app
COPY target/*.jar app.jar

#Exponer puerto
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]