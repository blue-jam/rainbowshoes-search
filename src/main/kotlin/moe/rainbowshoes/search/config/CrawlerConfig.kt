package moe.rainbowshoes.search.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.norconex.collector.core.filter.IReferenceFilter
import com.norconex.collector.core.filter.impl.ExtensionReferenceFilter
import com.norconex.collector.core.store.IDataStoreEngine
import com.norconex.collector.core.store.impl.jdbc.JdbcDataStoreEngine
import com.norconex.collector.http.HttpCollector
import com.norconex.collector.http.HttpCollectorConfig
import com.norconex.collector.http.crawler.HttpCrawlerConfig
import com.norconex.collector.http.delay.impl.GenericDelayResolver
import com.norconex.collector.http.link.ILinkExtractor
import com.norconex.collector.http.recrawl.impl.GenericRecrawlableResolver
import com.norconex.committer.core3.ICommitter
import com.norconex.committer.core3.fs.impl.JSONFileCommitter
import com.norconex.commons.lang.xml.XML
import com.norconex.importer.ImporterConfig
import com.norconex.importer.handler.filter.OnMatch
import moe.rainbowshoes.search.crawler.ConfigurableLinkExtractor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Paths

@Configuration
class CrawlerConfig {
    @Bean
    fun collectorConfig(
        httpCrawlerConfig: HttpCrawlerConfig,
        @Value("\${rainbowshoes.crawler.directory}") directory: String
    ): HttpCollectorConfig {
        val collectorConfig = HttpCollectorConfig()
        collectorConfig.id = "RainbowshoesSearchCollector"
        collectorConfig.workDir = Paths.get(directory)
        collectorConfig.setCrawlerConfigs(httpCrawlerConfig)

        return collectorConfig
    }

    @Bean
    fun httpCrawlerConfig(
        @Value("\${rainbowshoes.crawler.starturls}") startUrls: Array<String>,
        linkExtractors: Array<ILinkExtractor>,
        committers: Array<ICommitter>,
        referenceFilters: Array<IReferenceFilter>,
        delayResolver: GenericDelayResolver,
        genericRecrawlableResolver: GenericRecrawlableResolver,
        importerConfig: ImporterConfig,
        dataStoreEngine: IDataStoreEngine
    ): HttpCrawlerConfig {
        val crawlerConfig = HttpCrawlerConfig()
        crawlerConfig.id = "RainbowshoesSearchCrawler"
        crawlerConfig.setStartURLs(*startUrls)
        crawlerConfig.setLinkExtractors(*linkExtractors)
        crawlerConfig.setReferenceFilters(*referenceFilters)
        crawlerConfig.committers = committers.toList()
        crawlerConfig.delayResolver = delayResolver
        crawlerConfig.isIgnoreCanonicalLinks = true
        crawlerConfig.recrawlableResolver = genericRecrawlableResolver
        crawlerConfig.sitemapResolver = null
        crawlerConfig.dataStoreEngine = dataStoreEngine
        crawlerConfig.importerConfig = importerConfig

        return crawlerConfig
    }

    @Bean
    fun dataStoreEngine(@Value("\${rainbowshoes.crawler.directory}") crawlerDir: String): JdbcDataStoreEngine {
        val dbDirectory = Paths.get(crawlerDir, "work", "crawlstore", "jdbc", "h2")
        val url = "jdbc:h2:${dbDirectory.toAbsolutePath()}"

        val dataStoreEngine = JdbcDataStoreEngine()
        dataStoreEngine.configProperties.add("jdbcUrl", url)

        return dataStoreEngine
    }

    @Bean
    fun importerConfig(
        resourceLoader: ResourceLoader
    ): ImporterConfig {
        val fileUrl = resourceLoader.getResource("classpath:importerConfig.xml").url

        val config = ImporterConfig()
        config.loadFromXML(
            XML(InputStreamReader(fileUrl.openStream()))
        )

        return config
    }

    @Bean
    fun imageExcludeFilter() = ExtensionReferenceFilter(
        "png,gif,jpg,jpeg,pdf",
        OnMatch.EXCLUDE
    )

    @Bean
    fun delayResolver(
        @Value("\${rainbowshoes.crawler.delay.default:30000}") defaultDelayMillis: Long
    ): GenericDelayResolver {
        val delayResolver = GenericDelayResolver()
        delayResolver.defaultDelay = defaultDelayMillis

        return delayResolver
    }

    @Bean
    fun collector(collectorConfig: HttpCollectorConfig): HttpCollector {
        return HttpCollector(collectorConfig)
    }

    @Bean
    fun genericRecrawlableResolver(
        minFrequencies: Array<GenericRecrawlableResolver.MinFrequency>
    ): GenericRecrawlableResolver {
        val genericRecrawlableResolver = GenericRecrawlableResolver()
        genericRecrawlableResolver.setMinFrequencies(*minFrequencies)

        return genericRecrawlableResolver
    }

    @Bean
    fun minFrequency(
        @Value("\${rainbowshoes.crawler.serp.patterns}") serpPatterns: List<String>,
        @Value("\${rainbowshoes.crawler.serp.frequency:daily}") serpFrequency: String,
        @Value("\${rainbowshoes.crawler.default.frequency:weekly}") defaultFrequency: String
    ): Array<GenericRecrawlableResolver.MinFrequency> {
        val serpFrequencies = serpPatterns.map { pattern ->
            val minFrequency = GenericRecrawlableResolver.MinFrequency()
            minFrequency.pattern = pattern
            minFrequency.value = serpFrequency

            minFrequency
        }

        val defaultMinFrequency = GenericRecrawlableResolver.MinFrequency()
        defaultMinFrequency.pattern = ".*"
        defaultMinFrequency.value = defaultFrequency

        return (serpFrequencies + defaultMinFrequency).toTypedArray()
    }

    // @Bean
    fun jsonFileCommitter(
        @Value("\${rainbowshoes.crawler.json.docsPerFile:10000}") docsPerFile: Int,
        @Value("\${rainbowshoes.crawler.json.compress:true}") compress: Boolean
    ): JSONFileCommitter {
        val committer = JSONFileCommitter()

        committer.directory = Paths.get("build", "json")
        committer.docsPerFile = docsPerFile
        committer.isCompress = compress

        return committer
    }

    @Bean
    fun configurableLinkExtractors(
        resourceLoader: ResourceLoader,
        objectMapper: ObjectMapper
    ): Array<ConfigurableLinkExtractor> {
        val fileUrl = resourceLoader.getResource("classpath:linkExtractorConfig.json").url
        val configList: List<ConfigurableLinkExtractor.Config> = objectMapper.readValue(
            BufferedReader(InputStreamReader(fileUrl.openStream())).use {
                it.readText()
            }
        )

        return configList.map { ConfigurableLinkExtractor(it) }.toTypedArray()
    }
}
