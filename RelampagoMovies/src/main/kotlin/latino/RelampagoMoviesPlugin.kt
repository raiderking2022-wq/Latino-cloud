package latino

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class RelampagoMoviesPlugin : BasePlugin() {
    override fun load() { registerMainAPI(RelampagoMoviesProvider()) }
}
