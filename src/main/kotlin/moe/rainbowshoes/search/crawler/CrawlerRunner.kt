package moe.rainbowshoes.search.crawler

import com.norconex.collector.http.HttpCollector
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import javax.annotation.PreDestroy

@Component
class CrawlerRunner(
    val collector: HttpCollector,
//    val jsonFileCommitter: JSONFileCommitter
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        collector.start()
    }

    @PreDestroy
    fun destroy() {
//        jsonFileCommitter.commit()
        collector.stop()
    }
}
