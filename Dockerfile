# ---------- build stage: compile the WAR with Maven ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# Pre-compile every JSP: start the app, open each page once, stop it.
# Tomcat keeps the compiled pages in /src/tomcat/work, which we copy into the final image.
COPY docker/Warmup.java docker/Warmup.java
RUN (TOMCAT_BASEDIR=/src/tomcat PORT=8080 java -jar target/app.war > /tmp/app.log 2>&1 &) \
 && java docker/Warmup.java http://localhost:8080 || (cat /tmp/app.log; exit 1)

# ---------- run stage: just a JRE + the WAR (embedded Tomcat inside) ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/target/app.war app.war
COPY --from=build /src/tomcat/work /app/tomcat/work
ENV TOMCAT_BASEDIR=/app/tomcat
# Small-memory settings for a 512 MB free container
ENV JAVA_OPTS="-XX:MaxRAMPercentage=65 -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1 -Xshare:auto"
EXPOSE 8080
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.war"]
