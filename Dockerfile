FROM node:22-alpine AS frontend
WORKDIR /ui
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build
FROM maven:3.9.9-eclipse-temurin-17 AS backend
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY --from=frontend /src/main/resources/static ./src/main/resources/static
RUN mvn -B clean package
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S aura && adduser -S aura -G aura
COPY --from=backend /app/target/aura-voice-1.0.0.jar app.jar
USER aura
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
