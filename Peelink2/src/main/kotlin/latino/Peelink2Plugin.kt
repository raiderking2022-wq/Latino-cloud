package latino

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.extractors.Uqload

@CloudstreamPlugin
class Peelink2Plugin : BasePlugin() {
    override fun load() {
        registerMainAPI(Peelink2Provider())
        registerExtractorAPI(object : Uqload() {
            override var mainUrl = "https://uqload.vc"
        })
    }
}
