package latino

import org.jsoup.Jsoup
import org.junit.Assert.*
import org.junit.Test

class PeelinkCatalogTest {
    private val base = "https://www.peelink2.com"

    @Test fun emptySearchDoesNotBecomeLegacyLandingMovie() {
        val html = """
            <h3 class="post-title entry-title">
              <a href="http://www.peelinkp.com/2014/01/estrenos-cero-megavideo.html">Estrenos Cero Megavideo</a>
            </h3>
            <div class="post-body entry-content"><h2>No se ha encontrado</h2>
              <a href="/inicio"><img src="/banner.jpg"></a>
            </div>
            <aside><a href="/2024/01/ver-sidebar-2024.html"><img src="/sidebar.jpg"></a></aside>
        """.trimIndent()
        assertTrue(PeelinkCatalog.items(Jsoup.parse(html), base, search = true).isEmpty())
    }

    @Test fun readsNestedSearchPostersWithoutAltAndWithoutDuplicateOverlay() {
        // Shape observed on the live WordPress search page, including invalid nested anchors.
        val html = """
            <h3 class="post-title"><a href="/2014/01/estrenos-cero-megavideo.html">Estrenos Cero Megavideo</a></h3>
            <div class="post-body entry-content">
              <a href="$base/2021/12/ver-spider-man-sin-camino-a-casa-gratis.html"><div class="contenti">
                <div class="index"><img src="$base/poster.webp"></div>
                <a href="$base/2021/12/ver-spider-man-sin-camino-a-casa-gratis.html"><div class="ver"></div></a>
              </div></a>
            </div>
        """.trimIndent()
        val result = PeelinkCatalog.items(Jsoup.parse(html), base, search = true)
        assertEquals(1, result.size)
        assertEquals("Spider man sin camino a casa", result.single().title)
        assertEquals("$base/poster.webp", result.single().poster)
    }

    @Test fun separatesFeaturedFromFullCatalogAndKeepsYearFromMovieNotUpload() {
        val html = """
            <div class="post-body">
              <div class="contenedorh destacadas"><a href="/2026/10/ver-destacada-2026.html">
                <img src="/featured.jpg" alt="Ver Destacada (2026) Online"></a></div>
              <div class="contenedorh destacadas"><a href="/2026/10/ver-clasica-2001.html">
                <img src="/catalog.jpg" alt="Ver Clásica (2001) Online"></a></div>
            </div>
        """.trimIndent()
        val doc = Jsoup.parse(html)
        assertEquals("Clásica (2001)", PeelinkCatalog.items(doc, base).single().title)
        assertEquals(2001, PeelinkCatalog.items(doc, base).single().year)
        assertEquals("Destacada (2026)", PeelinkCatalog.items(doc, base, featured = true).single().title)
    }

    @Test fun paginationDoesNotConfusePageTwoWithPageTwenty() {
        assertEquals("$base/inicio/page/2", PeelinkCatalog.pageUrl("$base/inicio", 2))
        assertEquals("$base/inicio", PeelinkCatalog.pageUrl("$base/inicio#destacadas", 1))
        assertFalse(PeelinkCatalog.hasNext(Jsoup.parse("""<a href="/inicio/page/20">20</a>"""), 1))
        assertTrue(PeelinkCatalog.hasNext(Jsoup.parse("""<a href="/inicio/page/2/">2</a>"""), 1))
        assertFalse(PeelinkCatalog.hasNext(Jsoup.parse("<p>Última página</p>"), 286))
    }

    @Test fun normalizesSpiderManAndPreservesOtherQueries() {
        assertEquals(listOf("spider man", "spiderman"), PeelinkCatalog.searchTerms("spiderman"))
        assertEquals(listOf("spider man", "Spider-Man"), PeelinkCatalog.searchTerms("Spider-Man"))
        assertEquals(listOf("batman"), PeelinkCatalog.searchTerms("  batman  "))
        assertTrue(PeelinkCatalog.searchTerms(" ").isEmpty())
    }

    @Test fun readsGenreAjaxConfigurationAndPosterFragments() {
        val html = """
            <div id="list_posts"><a href="/2026/10/ver-ejemplo-2025.html"><img src="/poster.webp"></a></div>
            <button id="paginador" data-cat="11" data-cat-name="Accion" data-page="1"></button>
            <script>var request = {url: "https://peelink2.com/wp-content/themes/moviescript/parts/posts_pagina.php",
              data: {'post_id' : 576}};</script>
        """.trimIndent()
        val config = PeelinkCatalog.genrePage(Jsoup.parse(html), base)!!
        assertEquals("11", config.category)
        assertEquals("576", config.post)
        assertEquals("Accion", config.categoryName)
        assertEquals(1, PeelinkCatalog.items(Jsoup.parse(html), base).size)
        val fragment = Jsoup.parse("""<a href="/2026/10/ver-ejemplo-2025.html"><img src="/poster.webp"></a>""")
        assertEquals(1, PeelinkCatalog.items(fragment, base, fragment = true).size)
    }

    @Test fun excludesCategoryPagesAndForeignDomains() {
        val html = """
            <div class="post-body">
              <a href="/2012/12/pagina-accion.html"><img src="/banner.jpg"></a>
              <a href="https://unrelated.example/2024/01/ver-otro.html"><img src="/ad.jpg"></a>
              <a href="/2024/01/ver-buena-2024.html"><img data-src="/real.jpg" src="data:image/gif;base64,AAAA"></a>
            </div>
        """.trimIndent()
        val result = PeelinkCatalog.items(Jsoup.parse(html), base)
        assertEquals(1, result.size)
        assertEquals("$base/real.jpg", result.single().poster)
    }

    @Test fun recommendationsInsideSearchBodyAreNotSearchResults() {
        val html = """
            <div class="post-body">
              <div class="slider"><a href="/2026/01/ver-recomendacion-2026.html">
                <img src="/recommendation.jpg"></a></div>
              <a href="/2022/01/ver-batman-2022.html"><div class="contenti">
                <div class="index"><img src="/batman.jpg"></div>
                <a href="/2022/01/ver-batman-2022.html"><div class="ver"></div></a>
              </div></a>
            </div>
        """.trimIndent()
        val results = PeelinkCatalog.items(Jsoup.parse(html), base, search = true)
        assertEquals(1, results.size)
        assertEquals("Batman 2022", results.single().title)
        assertEquals("$base/batman.jpg", results.single().poster)
        val noResults = """<div class="post-body"><div class="slider">
            <a href="/2026/01/ver-recomendacion-2026.html"><img src="/ad.jpg"></a>
            </div><h2>No se ha encontrado</h2></div>"""
        assertTrue(PeelinkCatalog.items(Jsoup.parse(noResults), base, search = true).isEmpty())
    }
}
