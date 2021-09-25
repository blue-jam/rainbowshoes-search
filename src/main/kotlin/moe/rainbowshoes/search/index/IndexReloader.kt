package moe.rainbowshoes.search.index

import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.search.IndexSearcher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class IndexReloader(
    private val indexWriter: IndexWriter
) {
    private var indexSearcher: IndexSearcher = createIndexSearcher()

    fun getCurrentSearcher() = indexSearcher

    @Scheduled(fixedRateString = "\${rainbowshoes.index.reload.rate:3600000}")
    fun reload() {
        indexWriter.commit()
        indexSearcher = createIndexSearcher()
    }

    private fun createIndexSearcher() = IndexSearcher(DirectoryReader.open(indexWriter))
}
