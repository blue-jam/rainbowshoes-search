package moe.rainbowshoes.search.crawler

import com.norconex.collector.core.doc.CrawlDoc
import com.norconex.collector.http.link.ILinkExtractor
import com.norconex.collector.http.link.Link
import org.jsoup.Jsoup
import java.io.InputStream
import java.net.URL
import java.util.regex.Pattern

class ConfigurableLinkExtractor(private val config: Config) : ILinkExtractor {
    private val pathMatcher = Pattern.compile(config.pattern)

    data class Config(
        val host: String,
        val pattern: String,
        val linkSelectors: List<LinkSelector>
    ) {
        data class LinkSelector(
            val cssSelector: String,
            val attribute: String
        )
    }

    override fun extractLinks(doc: CrawlDoc?): MutableSet<Link> {
        if (!accepts(doc?.reference)) {
            return mutableSetOf()
        }

        return extractLinks(doc?.inputStream, doc?.reference)
    }

    private fun accepts(url: String?): Boolean {
        if (url == null) {
            return false
        }

        val urlObj = URL(url)
        val localUrl = urlObj.path +
            (if (urlObj.query == null) "" else urlObj.query)

        return urlObj.host == config.host && pathMatcher.matcher(localUrl).find()
    }

    private fun extractLinks(input: InputStream?, reference: String?): MutableSet<Link> {
        if (input == null || reference == null) {
            return mutableSetOf()
        }

        val document = Jsoup.parse(String(input.readAllBytes()), reference).normalise()

        return config.linkSelectors.flatMap { linkSelector ->
            val elements = document.select(linkSelector.cssSelector)

            elements.map { Link(it.absUrl(linkSelector.attribute)) }
        }.toMutableSet()
    }
}
