package moe.rainbowshoes.search.controller.debug

import com.norconex.collector.http.HttpCollector
import com.norconex.commons.lang.map.Properties
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.net.URL

@Controller
class ImporterTestController(
    private val collector: HttpCollector
) {
    @GetMapping("/debug/importerTest")
    @ResponseBody
    fun test(@RequestParam url: String): Properties {
        val urlObj = URL(url)
        val properties = Properties()
        collector.crawlers[0].importer.importDocument(urlObj.openStream(), properties, url)

        return properties
    }
}
