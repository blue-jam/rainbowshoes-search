# rainbowshoes-search
Search API for rainbowshoes.moe

# Develop

1. Add Git hooks
    ```sh
    ./init.sh
    ```

## Run with Gradle
1. Create directory for a crawler and a search index. Choose one from the below.
   1. Create directory in `/var/lib`
      ```sh
      sudo mkdir -p /var/lib/rainbowshoes-search && chown $(users) /var/lib/rainbowshoes-search
      ```
   1. Override `rainbowshoes.crawler.directory` and `rainbowshoes.lucene.directory` config. For example, create `~/.config/spring-boot/spring-boot-devtools.properties` and add these lines to the file:
      ```
      rainbowshoes.crawler.directory=build/crawler
      rainbowshoes.lucene.directory=build/lucene
      ```
      You can find other ways to override configs on
      [Spring Boot documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config).
1. Run
   ```sh
   ./gradlew bootRun
   ```

Then, the search API is available on http://localhost:8080/api/search?q=QUERY .

# Run with docker-compose

```sh
docker-compose up -d
```
