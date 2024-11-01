package jetzt.arbeitszeitbetrug.app.module.modules

import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

fun Application.contentNegotiationModule() {
    install(ContentNegotiation) {
        json()
    }
}