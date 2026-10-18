FROM eclipse-temurin:25-jdk AS builder

WORKDIR /build

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

COPY src src

RUN ./gradlew bootJar --no-daemon -x test && \
    cp $(find build/libs -name "*.jar" ! -name "*-plain.jar") app.jar

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN groupadd -r appgroup && useradd -r -g appgroup -d /app -s /usr/sbin/nologin appuser

COPY --from=builder --chown=appuser:appgroup /build/app.jar app.jar

USER appuser

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseSerialGC -Xms128m -Xmx384m -XX:MaxMetaspaceSize=128m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
