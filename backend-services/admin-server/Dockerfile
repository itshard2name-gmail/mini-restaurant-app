FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY admin-server/pom.xml admin-server/
COPY admin-server/src admin-server/src
RUN mvn -f admin-server/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre
VOLUME /tmp
COPY --from=build /app/admin-server/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
