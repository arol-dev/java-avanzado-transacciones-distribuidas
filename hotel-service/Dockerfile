# Etapa de build: empaquetar el microservicio con Maven
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# Copiamos los POM primero para cache
COPY . .
COPY pom.xml ./
COPY common/pom.xml common/pom.xml
COPY hotel-service/pom.xml hotel-service/pom.xml

# Fuentes necesarias (common + este módulo)
COPY common/src common/src
COPY hotel-service/src hotel-service/src

# Construcción del módulo
RUN mvn -DskipTests -pl hotel-service -am package

# Etapa de runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ENV JAVA_OPTS=""
COPY --from=builder /build/hotel-service/target/hotel-service-*.jar /app/app.jar
EXPOSE 8083
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]