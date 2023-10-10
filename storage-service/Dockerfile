FROM amazoncorretto:17-al2023-headless as builder

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew \
    && ./gradlew -p ./storage-service/server-component bootJar

FROM amazoncorretto:17-al2023-headless

WORKDIR /executable

COPY --from=builder /app/storage-service/server-component/build/libs/server-component-0.0.1-SNAPSHOT.jar .

VOLUME /storage

EXPOSE 8080

ENTRYPOINT java -jar ./server-component-0.0.1-SNAPSHOT.jar