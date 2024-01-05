FROM amazoncorretto:17-al2023-headless as builder

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew \
    && ./gradlew bootJar

FROM amazoncorretto:17-al2023-headless

WORKDIR /executable

COPY --from=builder /app/target-client/build/libs/target-client-0.0.1-SNAPSHOT.jar .

ENTRYPOINT tail -f /dev/null