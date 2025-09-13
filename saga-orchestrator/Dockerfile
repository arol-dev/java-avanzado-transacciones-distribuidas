# Etapa de build: empaquetar el microservicio con Maven
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# Copiamos los POM primero para cache de dependencias
COPY . .
COPY pom.xml ./
COPY common/pom.xml common/pom.xml
COPY saga-orchestrator/pom.xml saga-orchestrator/pom.xml

# Fuentes necesarias (common + este módulo)
COPY common/src common/src
COPY saga-orchestrator/src saga-orchestrator/src

# Construimos solo este módulo y sus dependencias
RUN mvn -DskipTests -pl saga-orchestrator -am package

# Etapa de runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ENV JAVA_OPTS=""
COPY --from=builder /build/saga-orchestrator/target/saga-orchestrator-*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]