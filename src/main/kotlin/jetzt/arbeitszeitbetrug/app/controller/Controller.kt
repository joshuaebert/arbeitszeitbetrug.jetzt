package jetzt.arbeitszeitbetrug.app.controller

import io.ktor.server.application.*
import io.ktor.server.request.*
import jetzt.arbeitszeitbetrug.app.util.Validator
import jetzt.arbeitszeitbetrug.app.util.ValidatorResult

/**
 * Passes responsibility to handle the call to the corresponding controller
 *
 * @param call: the call that should be passed
 * @param controller: the controller that should handle the call. Must suspend and have the
 *                    ApplicationCall as the only param
 */
suspend inline fun pass(call: ApplicationCall, controller: suspend (ApplicationCall) -> Unit) {
    controller.invoke(call)
}

/**
 * Passes responsibility to handle the call to the corresponding controller
 *
 * @param T: the type of the request body
 * @param call: the call that should be passed
 * @param validator: the validator that should be used to validate the request body/parameters
 * @param controller: the controller that should handle the call. Must suspend
 */
suspend inline fun <reified T : Any> pass(
    call: ApplicationCall,
    validator: Validator,
    controller: suspend (ApplicationCall, T) -> Unit
) {
    val receivedCall = call.receive<T>()

    val validationResult = validator.validate(call.parameters, receivedCall)
    if (validationResult is ValidatorResult.Error) {
        throw IllegalArgumentException("Validation failed")
    }
    controller.invoke(call, receivedCall)
}
