# ---------- build stage ----------
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src

RUN ./mvnw -B -q package -DskipTests

# ---------- runtime stage ----------
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/target/hw6-server-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
