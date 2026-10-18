package latino

import java.net.URI
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/** Pure parsing helpers: no network, JavaScript execution, CAPTCHA or login bypass. */
object HtmlTools {
    fun httpUrl(base: String, raw: String): String? = runCatching {
        val uri = URI(base).resolve(raw.trim().replace("&amp;", "&"))
        uri.takeIf {
            it.scheme in listOf("http", "https") && !it.host.isNullOrBlank() &&
                it.userInfo == null
        }?.toASCIIString()
    }.getOrNull()

    fun cleanTitle(raw: String): String = raw
        .replace(Regex("^\\s*(?:Ver|Descargar)\\s+", RegexOption.IGNORE_CASE), "")
        .replace(Regex("\\s+(?:(?:100%\\s+)?Online|Gratis|Full HD).*$", RegexOption.IGNORE_CASE), "")
        .trim()

    fun year(title: String): Int? =
        Regex("\\b(?:19|20)\\d{2}\\b").findAll(title).lastOrNull()?.value?.toIntOrNull()

    fun poster(node: Element, base: String): String? {
        val img = if (node.tagName() == "img") node else node.selectFirst("img")
        return img?.let {
            listOf("data-src", "data-lazy-src", "src").firstNotNullOfOrNull { attr ->
                it.attr(attr).takeIf(String::isNotBlank)?.let { value -> httpUrl(base, value) }
            }
        }
    }

    // Small decoder avoids android.util.Base64, java.util.Base64 (API 26) and extra runtime deps.
    fun decode64(value: String): String? = runCatching {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        val input = value.filterNot(Char::isWhitespace).replace('-', '+').replace('_', '/')
        require(input.isNotEmpty() && input.length <= 200_000)
        require(input.matches(Regex("[A-Za-z0-9+/]+={0,2}")))
        var buffer = 0
        var bits = 0
        val bytes = ArrayList<Byte>()
        input.takeWhile { it != '=' }.forEach { char ->
            buffer = (buffer shl 6) or alphabet.indexOf(char)
            bits += 6
            if (bits >= 8) {
                bits -= 8
                bytes.add(((buffer shr bits) and 255).toByte())
            }
        }
        bytes.toByteArray().toString(Charsets.UTF_8)
    }.getOrNull()

    /** The observed red2.php links store a URL as two nested base64 strings. */
    fun unwrapPlayer(url: String): String {
        if (!url.contains("/red2.php/")) return url
        var value = url.substringAfter("/red2.php/").substringBefore('?')
        repeat(3) {
            if (value.startsWith("https://") || value.startsWith("http://")) {
                return httpUrl(url, value) ?: url
            }
            value = decode64(value) ?: return url
        }
        return httpUrl(url, value)?.takeIf {
            value.startsWith("http://") || value.startsWith("https://")
        } ?: url
    }

    fun latinoPlayers(html: String, base: String): List<String> {
        val encoded = Regex(
            """px_repros\s*\[\s*['"]latino_\d+['"]\s*]\s*=\s*['"]([^'"]+)['"]""",
            RegexOption.IGNORE_CASE
        ).findAll(html).flatMap { match ->
            val fragment = decode64(match.groupValues[1]).orEmpty()
            Jsoup.parse(fragment).select("iframe[src], source[src], video[src]")
                .mapNotNull { httpUrl(base, it.attr("src")) }.asSequence()
        }.toList()
        // Never silently offer Spanish/Sub alternatives when latino options exist.
        if (encoded.isNotEmpty()) return encoded.map(::unwrapPlayer).distinct()
        val legacy = Regex("""video\s*\[\s*([12])\s*]\s*=\s*'([^']*)'""")
            .findAll(html).flatMap {
                Jsoup.parse(it.groupValues[2]).select("iframe[src]")
                    .mapNotNull { frame -> httpUrl(base, frame.attr("src")) }.asSequence()
            }.filterNot { it.endsWith("/red2.php/") }.map(::unwrapPlayer).distinct().toList()
        if (legacy.isNotEmpty()) return legacy
        return Jsoup.parse(html, base)
            .select("[data-lang=latino] iframe[src], .post-body iframe[src]")
            .mapNotNull { httpUrl(base, it.attr("src")) }.map(::unwrapPlayer).distinct()
    }

    fun mediaLinks(doc: Document): List<String> = doc
        .select("video[src], source[src], iframe[src], a[href]")
        .mapNotNull { node ->
            val value = if (node.hasAttr("src")) node.attr("src") else node.attr("href")
            httpUrl(doc.baseUri(), value)?.takeIf {
                node.tagName() != "a" ||
                    Regex("""\.(?:m3u8|mp4)(?:[?#]|$)""", RegexOption.IGNORE_CASE).containsMatchIn(it)
            }
        }.distinct()
}
