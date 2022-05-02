FROM amazoncorretto:11 AS builder

COPY . .
RUN ./gradlew build

FROM amazoncorretto:11 AS application
COPY --from=builder ./build/libs/rainbowshoes-search-0.0.1-SNAPSHOT.jar ./
EXPOSE 8080
RUN mkdir -p /var/lib/rainbowshoes-search && chown 1001 /var/lib/rainbowshoes-search
USER 1001
VOLUME /var/lib/rainbowshoes-search
CMD ["java", "-jar", "rainbowshoes-search-0.0.1-SNAPSHOT.jar"]
