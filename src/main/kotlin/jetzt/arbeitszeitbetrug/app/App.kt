package jetzt.arbeitszeitbetrug.app

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import jetzt.arbeitszeitbetrug.app.module.module
var currentlyActiveUsers = 0L

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}