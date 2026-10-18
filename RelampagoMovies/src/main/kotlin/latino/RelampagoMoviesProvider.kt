package latino

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import java.net.URI
import java.net.URLEncoder
import org.jsoup.nodes.Document

/**
 * Experimental: /movie-ID and ?page=N are indexed publicly, but live HTML is
 * Cloudflare-blocked in the development environment. Do not claim runtime validation.
 */
class RelampagoMoviesProvider : LatinoProvider() {
    override var mainUrl = "https://relampagomovies.com"
    override var name = "RelampagoMovies (experimental)"
    override val mainPage = mainPageOf(mainUrl to "Películas en latino")

    private fun cards(doc: Document): List<SearchResponse> = doc.select("a[href]")
        .filter { Regex("""(?:^|/)movie-\d+(?:[/?#]|$)""").containsMatchIn(it.attr("href")) }
        .sortedByDescending { it.selectFirst("img") != null }
        .mapNotNull(::card).distinctBy { it.url }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val doc = document("$mainUrl/?page=$page")
        val list = cards(doc)
        return newHomePageResponse(HomePageList(request.name, list), list.isNotEmpty())
    }

    override suspend fun search(query: String): List<SearchResponse> {
        // Discover the form instead of inventing an unverified search endpoint.
        val home = document(mainUrl)
        val form = home.select("form").firstOrNull { form ->
            form.select("input").any {
                it.attr("type") == "search" ||
                    it.attr("name") in listOf("q", "s", "search", "buscar", "busqueda", "query")
            }
        } ?: throw ErrorLoadingException(
            "No se pudo identificar el buscador de RelampagoMovies. Extensión experimental."
        )
        val input = form.select("input").first {
            it.attr("type") == "search" ||
                it.attr("name") in listOf("q", "s", "search", "buscar", "busqueda", "query")
        }
        val action = HtmlTools.httpUrl(mainUrl, form.attr("action")) ?: mainUrl
        require(URI(action).host in listOf(URI(mainUrl).host, "www.relampagomovies.com"))
        val values = form.select("input[type=hidden][name]")
            .associate { it.attr("name") to it.attr("value") }.toMutableMap()
        values[input.attr("name").ifBlank { "q" }] = query
        if (form.attr("method").equals("post", true)) {
            return cards(app.post(action, data = values).document)
        }
        val params = values.entries.joinToString("&") {
            "${URLEncoder.encode(it.key, "UTF-8")}=${URLEncoder.encode(it.value, "UTF-8")}"
        }
        return cards(document(action + (if ('?' in action) "&" else "?") + params))
    }

    override suspend fun load(url: String): LoadResponse = movie(url, document(url))

    override suspend fun loadLinks(
        data: String, isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean = resolvePlayers(
        HtmlTools.mediaLinks(document(data)), data, subtitleCallback, callback
    )
}
