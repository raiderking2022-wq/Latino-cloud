package latino

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import java.net.URLEncoder
import org.jsoup.nodes.Document

class Peelink2Provider : LatinoProvider() {
    override var mainUrl = "https://www.peelink2.com"
    override var name = "Peelink2 Latino"
    override val mainPage = mainPageOf(
        "$mainUrl/latino" to "Películas en latino",
        "$mainUrl/inicio" to "Últimas películas"
    )

    private fun cards(doc: Document): List<SearchResponse> = doc
        .select(".bloque a[href], .post-title a[href], .entry-title a[href]")
        .filter { Regex("""/\d{4}/\d{2}/[^/]+\.html""").containsMatchIn(it.attr("href")) }
        .mapNotNull(::card).distinctBy { it.url }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = request.data.trimEnd('/') + if (page > 1) "/page/$page" else ""
        val doc = document(url)
        return newHomePageResponse(
            HomePageList(request.name, cards(doc)),
            doc.select("a[href]").any { it.attr("href").contains("/page/${page + 1}") }
        )
    }

    override suspend fun search(query: String): List<SearchResponse> =
        cards(document("$mainUrl/?s=${URLEncoder.encode(query, "UTF-8")}"))

    override suspend fun load(url: String): LoadResponse = movie(url, document(url))

    override suspend fun loadLinks(
        data: String, isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val doc = document(data)
        return resolvePlayers(
            HtmlTools.latinoPlayers(doc.html(), data), data, subtitleCallback, callback
        )
    }
}
