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
RUN (WARMUP=false TOMCAT_BASEDIR=/src/tomcat PORT=8080 java -jar target/app.war > /tmp/app.log 2>&1 &) \
 && java docker/Warmup.java http://localhost:8080 || (cat /tmp/app.log; exit 1)

# ---------- run stage: just a JRE + the WAR (embedded Tomcat inside) ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/target/app.war app.war
COPY --from=build /src/tomcat/work /app/tomcat/work
ENV TOMCAT_BASEDIR=/app/tomcat
# Hard memory caps: heap 200 MB + metaspace + code cache stays well under the 512 MB free-tier limit
ENV JAVA_OPTS="-Xms48m -Xmx200m -XX:MaxMetaspaceSize=170m -XX:ReservedCodeCacheSize=48m -XX:MaxDirectMemorySize=32m -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1"
EXPOSE 8080
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.war"]
