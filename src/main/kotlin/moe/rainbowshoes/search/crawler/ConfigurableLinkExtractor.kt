package moe.rainbowshoes.search.crawler

import com.norconex.collector.http.url.ILinkExtractor
import com.norconex.collector.http.url.Link
import com.norconex.commons.lang.file.ContentType
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

    override fun extractLinks(input: InputStream?, reference: String?, contentType: ContentType?): MutableSet<Link> {
        if (input == null || reference == null) {
            return mutableSetOf()
        }

        val document = Jsoup.parse(String(input.readAllBytes()), reference).normalise()

        return config.linkSelectors.flatMap { linkSelector ->
            val elements = document.select(linkSelector.cssSelector)

            elements.map { Link(it.absUrl(linkSelector.attribute)) }
        }.toMutableSet()
    }

    override fun accepts(url: String?, contentType: ContentType?): Boolean {
        if (url == null) {
            return false
        }

        val urlObj = URL(url)

        return urlObj.host == config.host && pathMatcher.matcher(urlObj.path).find()
    }
}
