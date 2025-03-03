import godot.ProjectSettings
import kotlinx.serialization.json.Json
import java.io.File
import game.models.Data

class JsonDataParse(path: String) {
    val data: Data
    init {
        val finalPath = ProjectSettings.globalizePath(path)
        val text = File(finalPath).readText()
        data = Json.decodeFromString<Data>(text)
    }
}
