package jetzt.arbeitszeitbetrug.app.controller.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.logging.*
import io.ktor.utils.io.*
import jetzt.arbeitszeitbetrug.app.currentlyActiveUsers
import kotlinx.serialization.Serializable
import kotlin.time.TimeMark

object StartController {

    private val LOGGER = KtorSimpleLogger("StartController")

    @Serializable
    data class StartRequest(val endTime: String)

    suspend fun start(call: ApplicationCall, body: StartRequest) {
        LOGGER.debug("StartController.start called")
        currentlyActiveUsers++
        call.respond(HttpStatusCode.OK)
    }

}