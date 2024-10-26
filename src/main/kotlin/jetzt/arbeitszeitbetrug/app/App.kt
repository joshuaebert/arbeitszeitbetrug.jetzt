package jetzt.arbeitszeitbetrug.app

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.ratelimit.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(RateLimit)
    }.start(wait = true)
}