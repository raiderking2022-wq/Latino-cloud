package latino

import java.net.URI
import java.net.URLDecoder
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/** Select movie posters, never the legacy search-page heading or sidebar widgets. */
object PeelinkCatalog {
    data class Item(val title: String, val url: String, val poster: String?, val year: Int?)
    data class GenrePage(val endpoint: String, val category: String, val categoryName: String, val post: String)

    private fun canonicalMovie(base: String, raw: String): String? {
        val url = HtmlTools.httpUrl(base, raw) ?: return null
        val uri = URI(url)
        if (uri.host.removePrefix("www.") != URI(base).host.removePrefix("www.")) return null
        val path = uri.rawPath.orEmpty()
        if (!Regex("""^/\d{4}/\d{2}/[^/]+\.html$""").matches(path)) return null
        val slug = path.substringAfterLast('/')
        if (slug.startsWith("pagina-") || slug == "estrenos-cero-megavideo.html") return null
        return URI(base).resolve(path).toString()
    }

    fun items(doc: Document, base: String, featured: Boolean = false, fragment: Boolean = false): List<Item> {
        val grids = doc.select(".post-body .contenedorh.destacadas")
        val roots: List<Element> = when {
            featured -> listOfNotNull(grids.firstOrNull())
            grids.isNotEmpty() -> listOf(grids.last()!!)
            doc.selectFirst("#list_posts") != null -> listOf(doc.selectFirst("#list_posts")!!)
            fragment -> listOf(doc.body())
            else -> doc.select(".post-body, .entry-content").toList()
        }
        return roots.flatMap { root ->
            root.select("a[href]:has(img)").mapNotNull { link ->
                val url = canonicalMovie(base, link.attr("href")) ?: return@mapNotNull null
                val img = link.selectFirst("img") ?: return@mapNotNull null
                val rawTitle = listOf(img.attr("alt"), img.attr("title"), link.attr("title"))
                    .firstOrNull { it.isNotBlank() && !it.equals("Ver película", true) }
                    ?: URLDecoder.decode(URI(url).rawPath.substringAfterLast('/'), "UTF-8")
                        .removeSuffix(".html")
                        .replace(Regex("""^(?:(?:fx)?\d+-)+"""), "")
                        .replace(Regex("""^(?:ver-pelicula-|pelicula-)"""), "")
                        .replace('-', ' ')
                val title = HtmlTools.cleanTitle(rawTitle).replaceFirstChar { it.titlecase() }
                Item(title, url, HtmlTools.poster(img, base), HtmlTools.year(title))
            }
        }.distinctBy { it.url }
    }

    fun pageUrl(base: String, page: Int): String {
        require(page >= 1)
        val clean = base.substringBefore('#').trimEnd('/')
        return clean + if (page == 1) "" else "/page/$page"
    }

    fun hasNext(doc: Document, page: Int): Boolean =
        doc.select("a[href]").any { link ->
            Regex("""/page/${page + 1}(?:[/#?]|$)""").containsMatchIn(link.attr("href")) ||
                Regex("""[?&]paged=${page + 1}(?:[&#]|$)""").containsMatchIn(link.attr("href"))
        }

    fun searchTerms(query: String): List<String> {
        val original = query.trim().replace(Regex("""\s+"""), " ")
        if (original.isEmpty()) return emptyList()
        val normalized = original.replace(Regex("""spider[\s-]*man""", RegexOption.IGNORE_CASE), "spider man")
        return listOf(normalized, original).distinct()
    }

    fun genrePage(doc: Document, base: String): GenrePage? {
        val button = doc.selectFirst("#paginador[data-cat][data-cat-name]") ?: return null
        val html = doc.html()
        val endpoint = Regex("""url\s*:\s*["']([^"']*/parts/posts_pagina\.php)["']""")
            .find(html)?.groupValues?.get(1)?.let { HtmlTools.httpUrl(base, it) } ?: return null
        // Same site only; use the endpoint exposed by the site's own pagination button.
        if (URI(endpoint).host.removePrefix("www.") != URI(base).host.removePrefix("www.")) return null
        val post = Regex("""['"]post_id['"]\s*:\s*(\d+)""")
            .find(html)?.groupValues?.get(1) ?: return null
        return GenrePage(endpoint, button.attr("data-cat"), button.attr("data-cat-name"), post)
    }
}
