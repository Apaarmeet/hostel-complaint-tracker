# ---------- build stage: compile the WAR with Maven ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- run stage: just a JRE + the WAR (embedded Tomcat inside) ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/target/app.war app.war
# Small-memory settings for a 512 MB free container
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1"
EXPOSE 8080
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.war"]
