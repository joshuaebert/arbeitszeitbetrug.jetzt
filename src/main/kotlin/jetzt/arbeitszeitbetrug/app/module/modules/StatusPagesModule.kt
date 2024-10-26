package jetzt.arbeitszeitbetrug.app.module.modules

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.statusPageModule() {
    install(StatusPages) {
        handleRateLimit(this)
        handleException(this)
    }
}

fun handleRateLimit(config: StatusPagesConfig) {
    config.status(HttpStatusCode.TooManyRequests) { call, cause ->
        call.respondText("$cause", ContentType.Text.Plain)
    }
}

fun handleException(config: StatusPagesConfig) {
    config.exception<Throwable> { call, cause ->
        call.respondText("$cause", ContentType.Text.Plain)
    }
}