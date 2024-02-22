FROM amazoncorretto:17-al2023-headless as builder

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew \
    && ./gradlew -p ./rate-limit/rate-limit-server-component bootJar

FROM amazoncorretto:17-al2023-headless

WORKDIR /executable

COPY --from=builder /app/rate-limit/rate-limit-server-component/build/libs/rate-limit-server-component-0.0.1-SNAPSHOT.jar .

VOLUME /storage

EXPOSE 8080

ENTRYPOINT java -jar ./rate-limit-server-component-0.0.1-SNAPSHOT.jar