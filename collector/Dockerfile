FROM gradle:9.5.1-jdk17-alpine AS builder
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
RUN gradle dependencies --no-daemon || true
COPY src src
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
