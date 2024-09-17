FROM maven:3.9.5-eclipse-temurin-21-alpine AS builder
WORKDIR /application
COPY . .
RUN --mount=type=cache,target=/root/.m2  mvn clean install -Dmaven.test.skip

FROM eclipse-temurin:21.0.3_9-jre AS layers
WORKDIR /application
COPY --from=builder /application/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:21.0.3_9-jre
VOLUME /tmp
RUN useradd -ms /bin/bash spring-user
USER spring-user
COPY --from=layers /application/dependencies/ ./
COPY --from=layers /application/spring-boot-loader/ ./
COPY --from=layers /application/snapshot-dependencies/ ./
COPY --from=layers /application/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
