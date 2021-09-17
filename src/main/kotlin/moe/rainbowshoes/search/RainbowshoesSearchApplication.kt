package moe.rainbowshoes.search

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.time.Clock

@SpringBootApplication
class RainbowshoesSearchApplication {
	@Bean
	fun clock() = Clock.systemUTC()!!
}

fun main(args: Array<String>) {
	runApplication<RainbowshoesSearchApplication>(*args)
}
