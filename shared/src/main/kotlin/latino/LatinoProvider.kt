package latino

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.loadExtractor
import com.lagradost.cloudstream3.utils.newExtractorLink
import java.util.concurrent.CancellationException
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

abstract class LatinoProvider : MainAPI() {
    override var lang = "es"
    override val supportedTypes = setOf(TvType.Movie)
    override val hasMainPage = true

    protected suspend fun document(url: String): Document {
        val response = app.get(url)
        if (response.code in listOf(403, 503) ||
            response.text.contains("<title>Just a moment", ignoreCase = true)) {
            throw ErrorLoadingException(
                "$name bloquea el acceso con Cloudflare. Comprueba el sitio en el navegador; " +
                    "esta extensión no resuelve CAPTCHA ni garantiza acceso."
            )
        }
        if (response.code !in 200..299) {
            throw ErrorLoadingException("$name respondió HTTP ${response.code}.")
        }
        return response.document
    }

    protected fun card(link: Element): SearchResponse? {
        val url = HtmlTools.httpUrl(mainUrl, link.attr("href")) ?: return null
        val image = link.selectFirst("img")
        val rawTitle = listOf(
            image?.attr("alt").orEmpty(), image?.attr("title").orEmpty(),
            link.attr("title"), link.text()
        ).firstOrNull { it.isNotBlank() && !it.equals("Ver película", true) }
            ?: url.substringAfterLast('/').removeSuffix(".html").replace('-', ' ')
        return newMovieSearchResponse(HtmlTools.cleanTitle(rawTitle), url, TvType.Movie) {
            posterUrl = HtmlTools.poster(link, mainUrl)
            year = HtmlTools.year(rawTitle)
        }
    }

    protected suspend fun movie(url: String, doc: Document): LoadResponse {
        val title = HtmlTools.cleanTitle(
            doc.selectFirst("h1.post-title, h1.entry-title, h1, h2.post-title")?.text()
                ?: doc.selectFirst("meta[property=og:title]")?.attr("content")
                ?: doc.title().substringBefore(" | ").substringBefore(" - VER ")
        )
        return newMovieLoadResponse(title, url, TvType.Movie, url) {
            posterUrl = doc.selectFirst("meta[property=og:image]")?.attr("content")
                ?.let { HtmlTools.httpUrl(url, it) }
                ?: doc.selectFirst(".post-body img, .entry-content img, .poster img")
                    ?.let { HtmlTools.poster(it, url) }
            year = HtmlTools.year(title)
            plot = doc.selectFirst(".post-body > p, .entry-content > p, [itemprop=description]")
                ?.text()?.takeIf { it.isNotBlank() }
                ?: doc.selectFirst("meta[name=description]")?.attr("content")
        }
    }

    /** Registered Cloudstream extractors resolve video hosts; no ad/shortlink crawler. */
    protected suspend fun resolvePlayers(
        players: List<String>,
        referer: String,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val emitted = mutableSetOf<String>()
        val seen = mutableSetOf<String>()
        val counted: (ExtractorLink) -> Unit = { link ->
            if (emitted.add(link.url)) callback(link)
        }
        suspend fun resolve(raw: String, parent: String, depth: Int) {
            val url = HtmlTools.unwrapPlayer(raw)
            if (!seen.add(url) || seen.size > 24 || depth > 2) return
            if (Regex("""\.(?:m3u8|mp4)(?:[?#]|$)""", RegexOption.IGNORE_CASE)
                    .containsMatchIn(url)) {
                counted(newExtractorLink(name, "$name · Latino", url) {
                    this.referer = parent
                    quality = Qualities.Unknown.value
                })
                return
            }
            try {
                val before = emitted.size
                loadExtractor(url, parent, subtitleCallback, counted)
                if (emitted.size > before) return
                // Follow only an explicit player wrapper, not arbitrary linked pages.
                if (url.contains("/red2.php/")) {
                    HtmlTools.mediaLinks(document(url)).take(8).forEach {
                        resolve(it, url, depth + 1)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // One dead host must not prevent trying the next option.
            }
        }
        players.distinct().forEach { resolve(it, referer, 0) }
        if (emitted.isEmpty()) {
            throw ErrorLoadingException(
                "No se encontró un vídeo reproducible en $name. " +
                    "Puede faltar audio latino o el servidor no ser compatible con esta versión de Cloudstream."
            )
        }
        return true
    }
}
