package latino

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import org.jsoup.nodes.Document

class Peelink2Provider : LatinoProvider() {
    override var mainUrl = "https://www.peelink2.com"
    override var name = "Peelink2 Latino"
    override val mainPage = mainPageOf(
        "$mainUrl/inicio" to "Todo el catálogo",
        "$mainUrl/inicio#destacadas" to "Destacadas",
        "$mainUrl/latino" to "Películas en latino",
        "$mainUrl/2012/12/pagina-accion.html" to "Acción",
        "$mainUrl/2012/12/pagina-ficcion.html" to "Ciencia ficción",
        "$mainUrl/2012/12/pagina-drama.html" to "Drama",
        "$mainUrl/2012/12/pagina-comedia.html" to "Comedia",
        "$mainUrl/2012/12/pagina-infantil.html" to "Infantil",
        "$mainUrl/2012/12/pagina-terror.html" to "Terror"
    )
    private val genrePages = ConcurrentHashMap<String, PeelinkCatalog.GenrePage>()

    private fun cards(doc: Document, featured: Boolean = false, fragment: Boolean = false): List<SearchResponse> =
        PeelinkCatalog.items(doc, mainUrl, featured, fragment).map { item ->
            newMovieSearchResponse(item.title, item.url, TvType.Movie) {
                posterUrl = item.poster
                year = item.year
            }
        }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val featured = request.data.endsWith("#destacadas")
        val genre = request.data.contains("/pagina-")
        val doc: Document
        if (genre && page > 1) {
            val config = genrePages[request.data] ?: PeelinkCatalog.genrePage(
                document(request.data), mainUrl
            )?.also { genrePages[request.data] = it }
            if (config == null) return newHomePageResponse(HomePageList(request.name, emptyList()), false)
            val response = app.post(config.endpoint, data = mapOf(
                "paginador_page" to page.toString(), "cat_id" to config.category,
                "cat_name" to config.categoryName, "post_id" to config.post
            ))
            if (response.code !in 200..299) throw ErrorLoadingException("Peelink2 respondió HTTP ${response.code}.")
            doc = response.document
        } else {
            doc = document(PeelinkCatalog.pageUrl(request.data, page))
            if (genre) PeelinkCatalog.genrePage(doc, mainUrl)?.let { genrePages[request.data] = it }
        }
        return newHomePageResponse(
            HomePageList(request.name, cards(doc, featured, genre && page > 1)),
            if (genre) doc.selectFirst("#paginador") != null
            else !featured && PeelinkCatalog.hasNext(doc, page)
        )
    }

    private suspend fun searchPage(query: String, page: Int): Pair<List<SearchResponse>, Boolean> {
        for (term in PeelinkCatalog.searchTerms(query)) {
            val doc = document("$mainUrl/?s=${URLEncoder.encode(term, "UTF-8")}&paged=$page")
            val results = cards(doc)
            if (results.isNotEmpty()) return results to PeelinkCatalog.hasNext(doc, page)
        }
        return emptyList<SearchResponse>() to false
    }

    override suspend fun search(query: String): List<SearchResponse> = searchPage(query, 1).first

    override suspend fun search(query: String, page: Int): SearchResponseList {
        val (results, hasNext) = searchPage(query, page)
        return newSearchResponseList(results, hasNext)
    }

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
