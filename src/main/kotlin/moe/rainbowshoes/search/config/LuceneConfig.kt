package moe.rainbowshoes.search.config

import moe.rainbowshoes.search.index.IndexFields
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.ja.JapaneseAnalyzer
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.file.Paths

@Configuration
class LuceneConfig {
    @Bean
    fun directory(): Directory = FSDirectory.open(Paths.get("build", "index"))

    @Bean
    fun analyzer(): Analyzer = JapaneseAnalyzer()

    @Bean
    fun indexWriter(
        directory: Directory,
        analyzer: Analyzer
    ): IndexWriter {
        val indexWriterConfig = IndexWriterConfig(analyzer)
        indexWriterConfig.openMode = IndexWriterConfig.OpenMode.CREATE_OR_APPEND

        return IndexWriter(directory, indexWriterConfig)
    }

    @Bean
    fun queryParser(
        analyzer: Analyzer
    ) = StandardQueryParser(analyzer)
}
