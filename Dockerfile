# ---------------------------------------------------------------------------
# STAGE 1: Build & Packaging Environment (JDK)
# ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /build

ENV SBT_OPTS="-Dsbt.supershell=false -Dsbt.color=false"

RUN apt-get update && apt-get install -y curl gnupg && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.asdf.list && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt.list && \
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add && \
    apt-get update && apt-get install -y sbt

COPY build.sbt .
COPY project/ ./project/
RUN sbt update
COPY src/ ./src
RUN sbt -mem 2048 assembly

# ---------------------------------------------------------------------------
# STAGE 2: Pristine Production Runtime (JRE)
# ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN useradd -r -u 1001 -g root banking_worker
RUN mkdir -p /app/logs && \
        chown -R banking_worker:root /app
USER banking_worker

COPY --from=builder /build/target/scala-3.3.7/http-service.jar ./app.jar

EXPOSE 8081

# Run Java natively by relying on the internal JAR manifest configuration
ENTRYPOINT ["java", \
            "-XX:+UseG1GC", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-jar", "app.jar"]