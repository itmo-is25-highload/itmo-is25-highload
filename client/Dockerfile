FROM amazoncorretto:17-al2023-headless as builder

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew \
    && ./gradlew -p ./storage-service/server-component bootJar

FROM amazoncorretto:17-al2023-headless

WORKDIR /executable

COPY --from=builder /app/client/build/libs/client-0.0.1-SNAPSHOT.jar .

EXPOSE 8080

ENTRYPOINT tail -f /dev/null