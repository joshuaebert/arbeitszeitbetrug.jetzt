package jetzt.arbeitszeitbetrug.app.controller.v1

import io.ktor.server.application.*
import io.ktor.util.logging.*
import jetzt.arbeitszeitbetrug.app.currentlyActiveUsers

object StartController {

    private val LOGGER = KtorSimpleLogger("StartController")

    fun start(call: ApplicationCall, body: Unit) {
        LOGGER.debug("StartController.start called")
        currentlyActiveUsers++
    }

}