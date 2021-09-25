package moe.rainbowshoes.search

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling
import java.time.Clock

@SpringBootApplication
@EnableScheduling
class RainbowshoesSearchApplication {
    @Bean
    fun clock() = Clock.systemUTC()!!
}

fun main(args: Array<String>) {
    runApplication<RainbowshoesSearchApplication>(*args)
}
