FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY order-service/pom.xml order-service/
COPY order-service/src order-service/src
RUN mvn -f order-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre
VOLUME /tmp
COPY --from=build /app/order-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
