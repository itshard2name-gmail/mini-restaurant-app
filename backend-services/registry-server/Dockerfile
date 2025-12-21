FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY registry-server/pom.xml registry-server/
COPY registry-server/src registry-server/src
RUN mvn -f registry-server/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre
VOLUME /tmp
COPY --from=build /app/registry-server/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
