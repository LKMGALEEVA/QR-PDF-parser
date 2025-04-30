FROM maven:3.9.9-ibm-semeru-17-focal AS builder
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM openjdk:17-slim
WORKDIR /app

COPY --from=builder /app/target/QR-PDF-parser-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]