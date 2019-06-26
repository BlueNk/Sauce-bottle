package moe.bluenk.saucebottle

data class JsonFeeds(
    val header: Header,
    val results: List<Result>
)

data class Header(
    val account_type: String,
    val long_limit: String,
    val long_remaining: Int,
    val minimum_similarity: Double,
    val query_image: String,
    val query_image_display: String,
    val results_requested: Int,
    val results_returned: Int,
    val search_depth: String,
    val short_limit: String,
    val short_remaining: Int,
    val status: Int,
    val user_id: String
)

data class Result(
    val data: Data,
    val header: HeaderX
)

data class HeaderX(
    val index_id: Int,
    val index_name: String,
    val similarity: String,
    val thumbnail: String
)

data class Data(
    val characters: String,
    val creator: Any,
    val ext_urls: List<String>,
    val material: String,
    val sankaku_id: Int, // sankaku
    val source: String,
    val member_name: String, // pixiv
    val member_id: Int, // pixiv_id
    val title: String
)