package headout.oss.ergo.factory

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

/**
 * Created by shivanshs9 on 23/05/20.
 */
object JsonFactory {
    val json = Json(
        JsonConfiguration.Stable.copy(
            ignoreUnknownKeys = true
        )
    )
}