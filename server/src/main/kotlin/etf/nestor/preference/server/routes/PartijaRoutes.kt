package etf.nestor.preference.server.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import etf.nestor.preference.server.models.PartijaDownload
import etf.nestor.preference.server.models.PartijaSummary
import etf.nestor.preference.server.models.PartijaUpload
import etf.nestor.preference.server.models.addPartija
import etf.nestor.preference.server.models.hashPassword
import etf.nestor.preference.server.models.savedPartije

fun Route.partijaRouting() {
    route("/partije") {
        post {
            val upload = call.receive<PartijaUpload>()
            if (savedPartije.any { it.name == upload.name }) {
                return@post call.respondText(
                    "Partija sa tim imenom već postoji na serveru",
                    status = HttpStatusCode.Conflict
                )
            }
            val partija = addPartija(upload)
            call.respond(HttpStatusCode.Created, PartijaSummary(partija.id, partija.name))
        }
        get {
            call.respond(savedPartije.map { PartijaSummary(it.id, it.name) })
        }
        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respondText(
                "Greška nema id",
                status = HttpStatusCode.BadRequest
            )
            val password = call.request.queryParameters["password"] ?: return@get call.respondText(
                "Greška nema password",
                status = HttpStatusCode.BadRequest
            )
            val found = savedPartije.find { it.id == id }
            when {
                found == null -> call.respondText("Partija nije pronađena", status = HttpStatusCode.NotFound)
                found.password != hashPassword(password) -> call.respondText("Pogrešna lozinka", status = HttpStatusCode.Forbidden)
                else -> call.respond(PartijaDownload(found.name, found.savedAt, found.json))
            }
        }
    }
}
