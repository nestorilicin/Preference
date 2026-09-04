package etf.nestor.preference.server.plugins

import io.ktor.server.routing.*
import io.ktor.server.application.*
import etf.nestor.preference.server.routes.partijaRouting

fun Application.configureRouting() {
    routing {
        partijaRouting()
    }
}
