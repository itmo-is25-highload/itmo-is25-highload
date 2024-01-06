FROM amazoncorretto:17-al2023-headless as builder

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew \
    && ./gradlew -p ./target-service/target-service-server-component bootJar

FROM amazoncorretto:17-al2023-headless

WORKDIR /executable

COPY --from=builder /app/target-service/target-service-server-component/build/libs/target-service-server-component-0.0.1-SNAPSHOT.jar .

EXPOSE 8081

ENTRYPOINT tail -f /dev/null