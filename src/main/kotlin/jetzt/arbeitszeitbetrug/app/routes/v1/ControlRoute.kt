package jetzt.arbeitszeitbetrug.app.routes.v1

import io.ktor.server.routing.*
import jetzt.arbeitszeitbetrug.app.controller.pass
import jetzt.arbeitszeitbetrug.app.controller.v1.StartController
import jetzt.arbeitszeitbetrug.app.routes.API_PATH
import jetzt.arbeitszeitbetrug.app.routes.API_VERSION
import jetzt.arbeitszeitbetrug.app.util.Validator

const val START_ROUTE = "$API_PATH$API_VERSION/start"

fun Route.start() {
    post(START_ROUTE) {
        pass(call, Validator.None, StartController::start)
    }
}