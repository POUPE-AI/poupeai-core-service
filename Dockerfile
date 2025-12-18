FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .

COPY poupeai-core-application/pom.xml poupeai-core-application/
COPY poupeai-core-business/pom.xml poupeai-core-business/
COPY poupeai-core-domain/pom.xml poupeai-core-domain/
COPY poupeai-core-persistence/pom.xml poupeai-core-persistence/
COPY poupeai-core-security/pom.xml poupeai-core-security/
COPY poupeai-core-web/pom.xml poupeai-core-web/

RUN mvn dependency:go-offline -B

COPY poupeai-core-application/src poupeai-core-application/src
COPY poupeai-core-business/src poupeai-core-business/src
COPY poupeai-core-domain/src poupeai-core-domain/src
COPY poupeai-core-persistence/src poupeai-core-persistence/src
COPY poupeai-core-security/src poupeai-core-security/src
COPY poupeai-core-web/src poupeai-core-web/src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/poupeai-core-application/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]