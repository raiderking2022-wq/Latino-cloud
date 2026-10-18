package latino

import org.junit.Assert.*
import org.junit.Test

class HtmlToolsTest {
    @Test fun rejectsNonHttpAndCredentials() {
        assertNull(HtmlTools.httpUrl("https://example.org", "javascript:alert(1)"))
        assertNull(HtmlTools.httpUrl("https://example.org", "https://user:pass@example.org"))
        assertEquals("https://example.org/post", HtmlTools.httpUrl("https://example.org", "/post"))
    }

    @Test fun normalizesTitlesAndYear() {
        assertEquals("Ejemplo (2024)", HtmlTools.cleanTitle("Ver Ejemplo (2024) Online Gratis"))
        assertEquals(2024, HtmlTools.year("Ejemplo (2024)"))
    }

    @Test fun decodesUtf8AndRejectsInvalidInput() {
        assertEquals("Película", HtmlTools.decode64("UGVsw61jdWxh"))
        assertNull(HtmlTools.decode64("not base64!"))
    }

    @Test fun unwrapsObservedDoubleBase64Format() {
        assertEquals(
            "https://voe.sx/e/dvryrb2tpcwj",
            HtmlTools.unwrapPlayer(
                "https://www.peliculasrey.me/red2.php/YUhSMGNITTZMeTkyYjJVdWMzZ3ZaUzlrZG5KNWNtSXlkSEJqZDJvPQ=="
            )
        )
    }

    @Test fun onlySelectsLatinoWhenEncodedOptionsExist() {
        val html = """
            <script>
            px_repros['latino_0'] = 'PGlmcmFtZSBzcmM9Imh0dHBzOi8vZXhhbXBsZS5vcmcvbGF0aW5vIj48L2lmcmFtZT4=';
            px_repros['espanol_0'] = 'PGlmcmFtZSBzcmM9Imh0dHBzOi8vZXhhbXBsZS5vcmcvZXNwYW5vbCI+PC9pZnJhbWU+';
            </script>
        """.trimIndent()
        assertEquals(listOf("https://example.org/latino"), HtmlTools.latinoPlayers(html, "https://example.org"))
    }

    @Test fun ignoresEmptyLegacyPlayersAndDeduplicates() {
        val html = """<script>
            video[1] = '<iframe src="https://example.org/player"></iframe>';
            video[2] = '<iframe src="https://www.peliculasrey.me/red2.php/"></iframe>';
        </script>"""
        assertEquals(listOf("https://example.org/player"), HtmlTools.latinoPlayers(html, "https://example.org"))
    }

    @Test fun acceptsUppercaseIframeAttributes() {
        val html = """<script>px_repros['latino_0'] = 'PElGUkFNRSBTUkM9Imh0dHBzOi8vZXhhbXBsZS5vcmcvcGxheWVyIj48L0lGUkFNRT4=';</script>"""
        assertEquals(listOf("https://example.org/player"), HtmlTools.latinoPlayers(html, "https://example.org"))
    }
}
