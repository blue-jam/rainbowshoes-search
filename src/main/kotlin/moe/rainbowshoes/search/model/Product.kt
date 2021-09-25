package moe.rainbowshoes.search.model

data class Product(
    val title: String,
    val url: String,
    val relatedWorks: List<String>,
    val status: String?,
    val createdAt: Long?,
    val store: String?
)
