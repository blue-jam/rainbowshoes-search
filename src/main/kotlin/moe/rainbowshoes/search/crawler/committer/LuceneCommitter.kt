package moe.rainbowshoes.search.crawler.committer

import com.norconex.committer.core3.CommitterContext
import com.norconex.committer.core3.DeleteRequest
import com.norconex.committer.core3.ICommitter
import com.norconex.committer.core3.ICommitterRequest
import com.norconex.committer.core3.UpsertRequest
import com.norconex.commons.lang.map.Properties
import moe.rainbowshoes.search.index.IndexFields.CONTENT_FIELD
import moe.rainbowshoes.search.index.IndexFields.CREATED_AT_FIELD
import moe.rainbowshoes.search.index.IndexFields.CREATED_AT_STORED_FIELD
import moe.rainbowshoes.search.index.IndexFields.RELATED_WORK_FIELD
import moe.rainbowshoes.search.index.IndexFields.STATUS_FIELD
import moe.rainbowshoes.search.index.IndexFields.STORE_FIELD
import moe.rainbowshoes.search.index.IndexFields.TITLE_FIELD
import moe.rainbowshoes.search.index.IndexFields.URL_FIELD
import moe.rainbowshoes.search.index.IndexReloader
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.NumericDocValuesField
import org.apache.lucene.document.StoredField
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.Term
import org.apache.lucene.search.TermQuery
import org.springframework.stereotype.Component
import java.io.InputStream
import java.text.Normalizer
import java.time.Clock

@Component
class LuceneCommitter(
    private val indexWriter: IndexWriter,
    private val indexReloader: IndexReloader,
    private val clock: Clock
) : ICommitter {
//    @PreDestroy
//    fun destroy() {
//        indexWriter.commit()
//        indexWriter.close()
//    }

    fun normalizeText(text: String): String {
        return Normalizer.normalize(text, Normalizer.Form.NFKC)
    }

    override fun upsert(upsertRequest: UpsertRequest?) {
        add(upsertRequest?.reference, upsertRequest?.content, upsertRequest?.metadata)
    }

    fun add(reference: String?, content: InputStream?, metadata: Properties?) {
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
        val titleField = TextField(TITLE_FIELD, normalizeText(productName), Field.Store.YES)
        val relatedWorkFields = relatedWorks.map {
            StringField(RELATED_WORK_FIELD, normalizeText(it), Field.Store.YES)
        }
        val statusField = StringField(STATUS_FIELD, status, Field.Store.YES)
        val contentField = TextField(CONTENT_FIELD, normalizeText(pageContent), Field.Store.YES)

        val urlTerm = Term(URL_FIELD, reference)

        val indexSearcher = indexReloader.getCurrentSearcher()
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
        indexWriter.commit()
    }

    override fun close() {
        indexWriter.commit()
    }

    override fun init(committerContext: CommitterContext?) {
    }

    override fun accept(request: ICommitterRequest?): Boolean {
        return true
    }

    override fun delete(deleteRequest: DeleteRequest?) {
    }

    override fun clean() {
    }
}
