package jetzt.arbeitszeitbetrug.app.module

import io.ktor.server.application.*
import jetzt.arbeitszeitbetrug.app.module.modules.contentNegotiationModule
import jetzt.arbeitszeitbetrug.app.module.modules.rateLimitModule
import jetzt.arbeitszeitbetrug.app.module.modules.routingModule
import jetzt.arbeitszeitbetrug.app.module.modules.statusPageModule

fun Application.module() {
    statusPageModule()
    routingModule()
    rateLimitModule()
    contentNegotiationModule()
}