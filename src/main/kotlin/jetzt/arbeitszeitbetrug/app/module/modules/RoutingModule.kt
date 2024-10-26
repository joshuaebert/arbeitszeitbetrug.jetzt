package jetzt.arbeitszeitbetrug.app.module.modules

import io.ktor.server.application.*
import io.ktor.server.routing.*
import jetzt.arbeitszeitbetrug.app.routes.v1.start

fun Application.routingModule() {
    routing {
        start()
    }
}