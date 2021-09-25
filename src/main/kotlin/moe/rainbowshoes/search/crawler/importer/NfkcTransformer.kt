package moe.rainbowshoes.search.crawler.importer

import com.norconex.importer.doc.ImporterMetadata
import com.norconex.importer.handler.transformer.IDocumentTransformer
import java.io.InputStream
import java.io.OutputStream
import java.text.Normalizer

class NfkcTransformer : IDocumentTransformer {
    override fun transformDocument(
        reference: String?,
        input: InputStream?,
        output: OutputStream?,
        metadata: ImporterMetadata?,
        parsed: Boolean
    ) {
        val content = String(input!!.readAllBytes())
        val normalizedContent = Normalizer.normalize(content, Normalizer.Form.NFKC)
        output!!.write(normalizedContent.toByteArray())
    }
}
