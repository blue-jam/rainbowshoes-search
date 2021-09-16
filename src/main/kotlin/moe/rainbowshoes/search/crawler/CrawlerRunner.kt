package moe.rainbowshoes.search.crawler

import com.norconex.collector.http.HttpCollector
import com.norconex.committer.core.impl.JSONFileCommitter
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import javax.annotation.PreDestroy

@Component
class CrawlerRunner(
    val collector: HttpCollector,
    val jsonFileCommitter: JSONFileCommitter
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        collector.start(true)
    }

    @PreDestroy
    fun destroy() {
        jsonFileCommitter.commit()
        collector.stop()
    }
}
