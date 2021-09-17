package moe.rainbowshoes.search.crawler.committer

import com.norconex.committer.core.ICommitter
import com.norconex.commons.lang.map.Properties
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.NumericDocValuesField
import org.apache.lucene.document.StoredField
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.Term
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.TermQuery
import org.springframework.stereotype.Component
import java.io.InputStream
import java.time.Clock
import javax.annotation.PreDestroy

@Component
class LuceneCommitter(
    private val indexWriter: IndexWriter,
    private val indexSearcher: IndexSearcher,
    private val clock: Clock
) : ICommitter {
    companion object {
        const val URL_FIELD = "url"
        const val STORE_FIELD = "store"
        const val TITLE_FIELD = "title"
        const val RELATED_WORK_FIELD = "relatedWork"
        const val STATUS_FIELD = "status"
        const val CONTENT_FIELD = "content"
        const val CREATED_AT_FIELD = "createdAt"
        const val CREATED_AT_STORED_FIELD = "createdAtStored"
    }

    @PreDestroy
    fun destroy() {
        indexWriter.commit()
        indexWriter.close()
    }

    override fun add(reference: String?, content: InputStream?, metadata: Properties?) {
        if (reference == null || metadata == null) {
            return
        }

        val store = metadata["product.store"]?.firstOrNull() ?: ""
        val productName = metadata["product.name"]?.firstOrNull() ?: return
        val relatedWorks = metadata["product.relatedWork"] ?: listOf()
        val status = metadata["product.status"]?.firstOrNull() ?: "UNKNOWN"
        val pageContent = (metadata["product.content"] ?: listOf())
            .foldRight("") { left, next -> "$left\n\n$next" }

        val urlField = StringField(URL_FIELD, reference, Field.Store.YES)
        val storeField = StringField(STORE_FIELD, store, Field.Store.YES)
        val titleField = TextField(TITLE_FIELD, productName, Field.Store.YES)
        val relatedWorkFields = relatedWorks.map {
            StringField(RELATED_WORK_FIELD, it, Field.Store.YES)
        }
        val statusField = StringField(STATUS_FIELD, status, Field.Store.YES)
        val contentField = TextField(CONTENT_FIELD, pageContent, Field.Store.NO)

        val urlTerm = Term(URL_FIELD, reference)

        val hits = indexSearcher.search(TermQuery(urlTerm), 1).scoreDocs

        val document: Document
        if (hits.isEmpty()) {
            document = Document()
            document.add(urlField)

            val currentTimeMillis = clock.millis()
            document.add(
                NumericDocValuesField(CREATED_AT_FIELD, currentTimeMillis)
            )
            document.add(
                StoredField(CREATED_AT_STORED_FIELD, currentTimeMillis)
            )
        } else {
            document = indexSearcher.doc(hits.first().doc)
            document.removeField(STORE_FIELD)
            document.removeField(TITLE_FIELD)
            document.removeFields(RELATED_WORK_FIELD)
            document.removeField(STATUS_FIELD)
            document.removeField(CONTENT_FIELD)
        }
        document.add(storeField)
        document.add(titleField)
        relatedWorkFields.forEach {
            document.add(it)
        }
        document.add(statusField)
        document.add(contentField)

        indexWriter.updateDocument(urlTerm, document)
    }

    override fun remove(reference: String?, metadata: Properties?) {
    }

    override fun commit() {
        indexWriter.commit()
    }
}