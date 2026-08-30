FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B -ntp dependency:go-offline
COPY src/ src/
RUN ./mvnw -B -ntp -DskipTests package

FROM eclipse-temurin:21-jre
RUN addgroup --system irm && adduser --system --ingroup irm irm
WORKDIR /app
COPY --from=build /workspace/target/irm-api.jar /app/irm-api.jar
USER irm
EXPOSE 7001
ENTRYPOINT ["java", "-jar", "/app/irm-api.jar"]
