package jetzt.arbeitszeitbetrug.app.util

import io.ktor.http.*
import io.ktor.util.logging.*

val LOGGER = KtorSimpleLogger("Validator")

typealias Ok = ValidatorResult.Ok
typealias Error = ValidatorResult.Error
typealias ValidationChainDslInit<T> = ValidationChainDsl<T>.() -> Unit
typealias ParamContextDslInit = ParamContextDsl.() -> Unit
typealias BodyContextDslInit<T> = BodyContextDsl<T>.() -> Unit
typealias FieldContextDslInit<T> = FieldContextDsl<T>.() -> Unit

sealed class ValidatorResult {
    data object Ok : ValidatorResult()
    data class Error(val message: String) :  ValidatorResult()
}

@JvmInline
value class Allowed(val value: Boolean)

fun interface ValidatorType {
    fun validate(value: String): ValidatorResult
}

object Validators {

    fun length(min: Int, max: Int) = ValidatorType { value: String ->
        if (value.length in min..max) Ok else Error("Value not in range ($min, $max)")
    }

    fun notEmpty() = ValidatorType { value: String ->
        if (value.isNotEmpty()) ValidatorResult.Ok else Error("Value is empty")
    }

    fun isInt() = ValidatorType { value: String ->
        try {
            value.toInt()
            Ok
        } catch (e: NumberFormatException) {
            Error("Value is not an integer")
        }
    }
}

open class Validator(val validationChain: ValidationChainDsl<*>) {

    /**
     * Empty validator. Will validate nothing
     */
    companion object None : Validator(ValidationChainDsl<Any>())

    /**
     * Validates the parameters of a call
     *
     * @param params: the parameters to validate
     * @param values: the values to validate
     * @return ValidatorResult indicating whether the validation was successful
     */
    fun validateParams(params: List<ParamContextDsl>, values: Parameters): ValidatorResult {
        params.forEach { param ->
            val value = values[param.name] ?: return Error("Parameter not found")
            param.validators.forEach { (validator, allowed) ->
                val validationResult = validator.validate(value)

                if(validationResult is Ok && !allowed.value) {
                    LOGGER.error("Validation succeeded for a disallowed validator")
                    return Error("") //TODO: Add message
                }

                if (validationResult is Error) {
                    LOGGER.error("Validation failed for ${param.name}")
                    return validationResult
                }
            }
        }
        return Ok
    }

    /**
     * Validates the body of a call
     *
     * @param bodies: the bodies to validate
     * @param value: the value to validate
     * @return ValidatorResult indicating whether the validation was successful
     */
    inline fun <reified T : Any> validateBody(bodies: List<BodyContextDsl<T>>, value: T): ValidatorResult {
        bodies.forEach { body ->
            body.fields.forEach { field ->
                val actualValue = field.extractValue(value)
                field.validators.forEach { (validator, allowed) ->
                    val validationResult = validator.validate(actualValue)

                    if(validationResult is Ok && !allowed.value) {
                        LOGGER.error("Validation succeeded for a disallowed validator")
                        return Error("") //TODO: Add message
                    }

                    if (validationResult is Error) {
                        LOGGER.error("Validation failed for ${field.extractValue}")
                        return validationResult
                    }
                }
            }
        }
        return Ok
    }

    /**
     * Validates the parameters and body of a call
     *
     * @param parameters: the parameters to validate
     * @param body: the body to validate
     * @return ValidatorResult indicating whether the validation was successful
     */
    inline fun <reified T : Any> validate(parameters: Parameters, body: T): ValidatorResult {
        val paramValidationResult = validateParams(validationChain.params, parameters)
        val bodyValidationResult = validateBody<T>(validationChain.body as List<BodyContextDsl<T>>, body)

        return if (paramValidationResult is Ok && bodyValidationResult is Ok) {
            Ok
        } else {
            Error("Validation failed")
        }
    }
}

@JvmName("validatorChain0")
inline fun <reified T : Any> validatorChain(
    validatorChain: ValidationChainDsl<T> = ValidationChainDsl(),
    init: ValidationChainDslInit<T>
): Validator {
    val chain = validatorChain.apply(init)
    return Validator(chain)
}

@JvmName("validatorChain1")
inline fun validatorChain(
    validatorChain: ValidationChainDsl<Any> = ValidationChainDsl(),
    init: ValidationChainDslInit<Any>
) : Validator {
    val chain = validatorChain.apply(init)
    return Validator(chain)
}

class ValidationChainDsl<T> {
    val params = mutableListOf<ParamContextDsl>()
    val body = mutableListOf<BodyContextDsl<T>>()

    inline fun param(name: String, paramContext: ParamContextDsl = ParamContextDsl(name), init: ParamContextDslInit) {
        val p = ParamContextDsl(name).apply(init)
        params.add(p)
    }

    inline fun body(bodyContext: BodyContextDsl<T> = BodyContextDsl(), init: BodyContextDslInit<T>) {
        val b = BodyContextDsl<T>().apply(init)
        body.add(b)
    }
}

class ParamContextDsl(val name: String, val validators: MutableMap<ValidatorType, Allowed> = mutableMapOf()) {
    operator fun ValidatorType.unaryPlus() {
        validators[this] = Allowed(true)
    }

    operator fun ValidatorType.unaryMinus() {
        validators[this] = Allowed(false)
    }
}

class BodyContextDsl<T> {
    val fields = mutableListOf<FieldContextDsl<T>>()

    fun field(extractValue: (T) -> String, init: FieldContextDslInit<T>) {
        val fieldContext = FieldContextDsl(extractValue).apply(init)
        fields.add(fieldContext)
    }
}

class FieldContextDsl<T>(val extractValue: (T) -> String, val validators: MutableMap<ValidatorType, Allowed> = mutableMapOf()) {
    operator fun ValidatorType.unaryPlus() {
        validators[this] = Allowed(true)
    }

    operator fun ValidatorType.unaryMinus() {
        validators[this] = Allowed(false)
    }
}