FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY gateway-service/pom.xml gateway-service/
COPY gateway-service/src gateway-service/src
# Gateway needs discovery client, so it might need registry-server available? No, build time is fine.
RUN mvn -f gateway-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre
VOLUME /tmp
COPY --from=build /app/gateway-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
