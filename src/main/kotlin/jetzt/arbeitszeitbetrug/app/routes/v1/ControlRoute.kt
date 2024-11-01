package jetzt.arbeitszeitbetrug.app.routes.v1

import io.ktor.server.routing.*
import jetzt.arbeitszeitbetrug.app.controller.pass
import jetzt.arbeitszeitbetrug.app.controller.v1.StartController
import jetzt.arbeitszeitbetrug.app.routes.API_PATH
import jetzt.arbeitszeitbetrug.app.routes.API_VERSION
import jetzt.arbeitszeitbetrug.app.util.Validator
import jetzt.arbeitszeitbetrug.app.util.Validators.isTime
import jetzt.arbeitszeitbetrug.app.util.Validators.notEmpty
import jetzt.arbeitszeitbetrug.app.util.validatorChain

const val START_ROUTE = "$API_PATH$API_VERSION/start"

val startValidator = validatorChain<StartController.StartRequest> {
    body {
        field({ it.endTime }) {
            +notEmpty()
            +isTime()
        }
    }
}

fun Route.start() {
    post(START_ROUTE) {
        pass(call, startValidator, StartController::start)
    }
}