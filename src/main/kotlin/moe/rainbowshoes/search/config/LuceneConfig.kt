package moe.rainbowshoes.search.config

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseAnalyzer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.file.Paths

@Configuration
class LuceneConfig {
    @Bean
    fun directory(@Value("\${rainbowshoes.lucene.directory}") directory: String): Directory =
        FSDirectory.open(Paths.get(directory))

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
    ): StandardQueryParser {
        val parser = StandardQueryParser(analyzer)
        parser.defaultOperator = StandardQueryConfigHandler.Operator.AND

        return parser
    }
}
