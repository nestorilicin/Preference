package etf.nestor.preference.server.models

import etf.nestor.preference.server.storage.loadPartije
import etf.nestor.preference.server.storage.savePartije
import kotlinx.serialization.Serializable
import java.security.MessageDigest

@Serializable
data class PartijaUpload(val password: String, val name: String?, val savedAt: Long, val json: String)

@Serializable
data class Partija(val id: Int, val password: String, val name: String?, val savedAt: Long, val json: String)

@Serializable
data class PartijaSummary(val id: Int, val name: String?)

@Serializable
data class PartijaDownload(val name: String?, val savedAt: Long, val json: String)

val savedPartije: MutableList<Partija> = loadPartije()
private var nextId = (savedPartije.maxOfOrNull { it.id } ?: 0) + 1

fun addPartija(upload: PartijaUpload): Partija {
    val partija = Partija(
        id = nextId++,
        password = hashPassword(upload.password),
        name = upload.name,
        savedAt = upload.savedAt,
        json = upload.json
    )
    savedPartije.add(partija)
    savePartije(savedPartije)
    return partija
}

fun hashPassword(password: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
    return digest.joinToString("") { "%02x".format(it) }
}
