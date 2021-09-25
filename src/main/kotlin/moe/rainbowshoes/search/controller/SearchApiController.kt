package moe.rainbowshoes.search.controller

import moe.rainbowshoes.search.index.IndexFields
import moe.rainbowshoes.search.index.IndexReloader
import moe.rainbowshoes.search.model.Product
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.queryparser.flexible.core.QueryNodeException
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser
import org.apache.lucene.search.TopScoreDocCollector
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class SearchApiController(
    val indexReloader: IndexReloader,
    val queryParser: StandardQueryParser
) {
    data class Response(
        val products: List<Product>,
        val total: Long
    )

    @GetMapping("/api/search")
    @ResponseBody
    fun search(
        @RequestParam(required = true, name = "q") queryString: String,
        @RequestParam(required = true, name = "p", defaultValue = "0") page: Int,
        @RequestParam(required = true, name = "n", defaultValue = "20") numPerPage: Int
    ): Response {
        val searcher = indexReloader.getCurrentSearcher()
        val query = try {
            queryParser.parse(queryString, IndexFields.CONTENT_FIELD)
        } catch (e: QueryNodeException) {
            queryParser.parse(QueryParser.escape(queryString), IndexFields.CONTENT_FIELD)
        }

        val collector = TopScoreDocCollector.create(numPerPage * (page + 1), 10000)

        searcher.search(query, collector)
        val topDocs = collector.topDocs(page * numPerPage, numPerPage)

        val products = topDocs.scoreDocs.map { scoreDoc ->
            searcher.doc(scoreDoc.doc)
        }
            .map { doc ->
                Product(
                    doc.get(IndexFields.TITLE_FIELD),
                    doc.get(IndexFields.URL_FIELD),
                    doc.getValues(IndexFields.RELATED_WORK_FIELD).toList(),
                    doc.get(IndexFields.STATUS_FIELD),
                    doc.get(IndexFields.CREATED_AT_STORED_FIELD)?.toLong()
                )
            }

        return Response(products, topDocs.totalHits.value)
    }
}
