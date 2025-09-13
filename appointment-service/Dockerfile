# Etapa de build: empaquetar el microservicio con Maven
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# Copiamos los POM primero para aprovechar la cache de dependencias
COPY . .
COPY pom.xml ./
COPY common/pom.xml common/pom.xml
COPY appointment-service/pom.xml appointment-service/pom.xml

# Copiamos el código fuente necesario (módulo común y el propio)
COPY common/src common/src
COPY appointment-service/src appointment-service/src

# Construimos solo este módulo y sus dependencias (-am)
RUN mvn -DskipTests -pl appointment-service -am package

# Etapa de runtime: solo JRE y el JAR final
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ENV JAVA_OPTS=""
COPY --from=builder /build/appointment-service/target/appointment-service-*.jar /app/app.jar
EXPOSE 8081
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]