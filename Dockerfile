FROM eclipse-temurin:21-jdk AS build
ARG SERVICE
WORKDIR /workspace
COPY . .
RUN chmod +x ${SERVICE}/mvnw && cd ${SERVICE} && ./mvnw -q -DskipTests package

FROM eclipse-temurin:21-jre
ARG SERVICE
WORKDIR /app
COPY --from=build /workspace/${SERVICE}/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
