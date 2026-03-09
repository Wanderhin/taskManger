# ─────────────────────────────────────────────
# STAGE 1 : Build
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY pom.xml .
RUN apt-get update && apt-get install -y maven && \
    mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ─────────────────────────────────────────────
# STAGE 2 : Runtime (image légère)
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre

WORKDIR /app

RUN groupadd -r taskmanager && useradd -r -g taskmanager taskmanager

COPY --from=build /app/target/*.jar app.jar

RUN chown taskmanager:taskmanager app.jar

USER taskmanager

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]


