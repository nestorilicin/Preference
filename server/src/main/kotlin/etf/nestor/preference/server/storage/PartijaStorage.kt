package etf.nestor.preference.server.storage

import etf.nestor.preference.server.models.Partija
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

private val storageFile = File("data/partije.json")
private val json = Json { prettyPrint = true }

fun loadPartije(): MutableList<Partija> {
    if (!storageFile.exists()) return mutableListOf()
    return try {
        json.decodeFromString<List<Partija>>(storageFile.readText()).toMutableList()
    } catch (_: Exception) {
        mutableListOf()
    }
}

fun savePartije(partije: List<Partija>) {
    storageFile.parentFile?.mkdirs()
    storageFile.writeText(json.encodeToString(partije))
}
