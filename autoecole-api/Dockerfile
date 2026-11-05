# Image de build : JDK 21 + Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache des dépendances
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B -DskipTests package

# Image d'exécution : JRE seul
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
