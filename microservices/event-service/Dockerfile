FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /workspace
COPY microservices/event-contract ./microservices/event-contract
COPY microservices/event-service ./microservices/event-service
RUN mvn -f microservices/event-contract/pom.xml -DskipTests install && \
    mvn -f microservices/event-service/pom.xml -DskipTests clean package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /workspace/microservices/event-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]
