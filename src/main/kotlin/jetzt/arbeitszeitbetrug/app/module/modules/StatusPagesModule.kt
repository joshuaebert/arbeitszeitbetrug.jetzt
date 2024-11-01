package jetzt.arbeitszeitbetrug.app.module.modules

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.statusPageModule() {
    install(StatusPages) {
        handleRateLimit()
        handleException()
        handleOk()
    }
}

fun StatusPagesConfig.handleRateLimit() {
    status(HttpStatusCode.TooManyRequests) { call, cause ->
        call.respondText("$cause", ContentType.Text.Plain)
    }
}

fun StatusPagesConfig.handleException() {
    exception<Throwable> { call, cause ->
        call.respondText("$cause", ContentType.Text.Plain)
    }
}

fun StatusPagesConfig.handleOk() {
    status(HttpStatusCode.OK) { call, cause ->
        call.respondText("$cause", ContentType.Text.Plain)
    }
}