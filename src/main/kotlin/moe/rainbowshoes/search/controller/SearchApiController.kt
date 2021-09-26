package moe.rainbowshoes.search.controller

import moe.rainbowshoes.search.index.IndexFields
import moe.rainbowshoes.search.index.IndexReloader
import moe.rainbowshoes.search.model.Product
import org.apache.lucene.index.Term
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.queryparser.flexible.core.QueryNodeException
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.Query
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TopScoreDocCollector
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class SearchApiController(
    val indexReloader: IndexReloader,
    val queryParser: StandardQueryParser
) {
    companion object {
        val AVAILABLE_STATUS_LIST = listOf("ON_SALE", "ACCEPTING_RESERVATION", "UNKNOWN")
    }

    data class Response(
        val products: List<Product>,
        val total: Long
    )

    @CrossOrigin(origins = ["localhost", "rainbowshoes.moe"])
    @GetMapping("/api/search")
    @ResponseBody
    fun search(
        @RequestParam(name = "q") queryString: String,
        @RequestParam(name = "p", defaultValue = "0") page: Int,
        @RequestParam(name = "n", defaultValue = "20") numPerPage: Int,
        @RequestParam(name = "status", defaultValue = "") statusFilterName: String
    ): Response {
        val searcher = indexReloader.getCurrentSearcher()
        val userQuery = try {
            queryParser.parse(queryString, IndexFields.CONTENT_FIELD)
        } catch (e: QueryNodeException) {
            queryParser.parse(QueryParser.escape(queryString), IndexFields.CONTENT_FIELD)
        }

        val queryBuilder = BooleanQuery.Builder()
            .add(userQuery, BooleanClause.Occur.MUST)

        val statusFilterQuery = buildStatusFilter(statusFilterName)
        statusFilterQuery?.run {
            queryBuilder.add(statusFilterQuery, BooleanClause.Occur.FILTER)
        }

        val collector = TopScoreDocCollector.create(numPerPage * (page + 1), 10000)

        val query = queryBuilder.build()
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
                    doc.get(IndexFields.CREATED_AT_STORED_FIELD)?.toLong(),
                    doc.get(IndexFields.STORE_FIELD)
                )
            }

        return Response(products, topDocs.totalHits.value)
    }

    fun buildStatusFilter(
        statusFilterName: String
    ): Query? {
        return when (statusFilterName) {
            "ALL" -> null
            else -> {
                val builder = BooleanQuery.Builder()
                    .setMinimumNumberShouldMatch(1)
                AVAILABLE_STATUS_LIST.forEach { status ->
                    builder.add(
                        TermQuery(Term(IndexFields.STATUS_FIELD, status)),
                        BooleanClause.Occur.SHOULD
                    )
                }

                builder.build()
            }
        }
    }
}
