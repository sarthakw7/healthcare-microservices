# ---- Build Stage ----
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests

# ---- Run Stage ----
FROM openjdk:21-jdk AS runner

WORKDIR /app

COPY --from=builder ./app/target/*.jar app.jar

EXPOSE 4002

ENTRYPOINT ["java", "-jar", "app.jar"]
