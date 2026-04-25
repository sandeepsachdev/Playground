FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B -DskipTests package \
    && mv target/*.jar app.jar

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN useradd --system --uid 1001 --shell /usr/sbin/nologin spring
USER spring

COPY --from=build /workspace/app.jar /app/app.jar

# Leave headroom for non-heap (Metaspace, JIT code cache, direct byte buffers,
# thread stacks, native HTTP client state). At 75% of a 512 MB container the
# heap was 384 MB and only ~128 MB was left for everything else, which Spring
# Boot 3.2 + JPA + Rome routinely overrun after a few hours.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=60.0 \
               -XX:MaxMetaspaceSize=192m \
               -XX:ReservedCodeCacheSize=96m \
               -XX:MaxDirectMemorySize=64m \
               -XX:+UseSerialGC \
               -Xss256k \
               -XX:+ExitOnOutOfMemoryError \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/tmp/heap.hprof"
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=${PORT} -jar /app/app.jar"]
