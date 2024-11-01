package jetzt.arbeitszeitbetrug.app.util

import io.ktor.http.*
import io.ktor.util.logging.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException
import kotlin.time.TimeMark

val LOGGER = KtorSimpleLogger("Validator")

typealias Ok = ValidatorResult.Ok
typealias Error = ValidatorResult.Error
typealias ValidationChainDslInit<T> = ValidationChainDsl<T>.() -> Unit
typealias ParamContextDslInit = ParamContextDsl.() -> Unit
typealias BodyContextDslInit<T> = BodyContextDsl<T>.() -> Unit
typealias FieldContextDslInit<T> = FieldContextDsl<T>.() -> Unit

sealed class ValidatorResult {
    data object Ok : ValidatorResult()
    data class Error(val message: String) : ValidatorResult()
}

@JvmInline
value class Allowed(val value: Boolean)

data class ValidationInfo(val type: ValidatorType, var allowed: Allowed)

fun interface ValidatorType {
    fun validate(value: String): ValidatorResult
}

object Validators {

    /**
     * Checks if the length of the value is in an inclusive range
     * @param min: minimum length
     * @param max: maximum length
     */
    fun length(min: Int, max: Int) = ValidatorType { value: String ->
        if (value.length in min..max) Ok else Error("Value not in range: [$min, $max]")
    }

    /**
     * Checks if the value is not empty
     */
    fun notEmpty() = ValidatorType { value: String ->
        if (value.isNotEmpty()) ValidatorResult.Ok else Error("Value is empty")
    }

    fun isTime() = ValidatorType {value: String ->
        try {
            LocalTime.parse(value)
            Ok
        } catch (e: DateTimeParseException) {
            Error("Value is not a time")
        }
    }

    /**
     * Checks if the value is an integer
     */
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
            val actualValue = values[param.name] ?: return Error("Parameter not found")
            return validateInput(param.validators, actualValue)
        }
        return Ok
    }

    /**
     * Validates the body of a call
     *
     * @param body: the body to validate
     * @param value: the value to validate
     * @return ValidatorResult indicating whether the validation was successful
     */
    inline fun <reified T : Any> validateBody(body: BodyContextDsl<T>, value: T): ValidatorResult {
        body.fields.forEach { field ->
            val actualValue = field.extractValue.run {
                val invokeResult = runCatching {
                    invoke(value)
                }
                if (invokeResult.isFailure) {
                    return Error("Given type is not the expected (Expected: ${T::class.java.name})")
                }
                return@run invokeResult.getOrNull() ?: return Error("Value is null")
            }
            return validateInput(field.validators, actualValue)
        }
        return Ok
    }

    fun validateInput(validationInfo: List<ValidationInfo>, actualValue: String): ValidatorResult {
        validationInfo.forEach { (validator, allowed) ->
            val validationResult = validator.validate(actualValue)

            if (validationResult is Ok && !allowed.value) {
                LOGGER.error("Validation succeeded for a disallowed validator")
                return Error("") //TODO: Add message
            }

            if (validationResult is Error) {
                LOGGER.error("Validation failed for $actualValue")
                return validationResult
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
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any> validate(parameters: Parameters, body: T): ValidatorResult {
        val paramValidationResult = validateParams(validationChain.params, parameters)
        //TODO: cast may not succeed. Add check
        val bodyValidationResult = validateBody<T>(validationChain.body as BodyContextDsl<T>, body)

        return if (paramValidationResult is Ok && bodyValidationResult is Ok) {
            Ok
        } else {
            Error("Validation failed")
        }
    }
}

/**
 * Creates a new validation chain for param & body validation
 * @param T: the expected body type
 * @return a validator
 */
@JvmName("validatorChain0")
inline fun <reified T : Any> validatorChain(
    validatorChain: ValidationChainDsl<T> = ValidationChainDsl(),
    init: ValidationChainDslInit<T>
): Validator {
    val chain = validatorChain.apply(init)
    return Validator(chain)
}

/**
 * Creates a new validation chain for param validation
 * @return a validator
 */
@JvmName("validatorChain1")
inline fun validatorChain(
    validatorChain: ValidationChainDsl<Any> = ValidationChainDsl(),
    init: ValidationChainDslInit<Any>
): Validator {
    val chain = validatorChain.apply(init)
    return Validator(chain)
}

class ValidationChainDsl<T> {
    val params = mutableListOf<ParamContextDsl>()
    var body = BodyContextDsl<T>()

    /**
     * Adds a param to the validation chain
     */
    inline fun param(name: String, paramContext: ParamContextDsl = ParamContextDsl(name), init: ParamContextDslInit) {
        val p = ParamContextDsl(name).apply(init)
        params.add(p)
    }

    /**
     * Adds a body to the validation chain
     */
    inline fun body(bodyContext: BodyContextDsl<T> = BodyContextDsl(), init: BodyContextDslInit<T>) {
        val b = BodyContextDsl<T>().apply(init)
        body = b
    }
}

class ParamContextDsl(val name: String, val validators: ArrayList<ValidationInfo> = arrayListOf()) {

    /**
     * Adds a validator to the param context
     */
    operator fun ValidatorType.unaryPlus() {
        validators.add(ValidationInfo(this, Allowed(true)))
    }

    /**
     * Adds a disallowed validator to the param context
     */
    operator fun ValidatorType.unaryMinus() {
        validators.add(ValidationInfo(this, Allowed(false)))
    }
}

class BodyContextDsl<T> {
    val fields = mutableListOf<FieldContextDsl<T>>()

    /**
     * Adds a field to the body context
     * @param extractValue: the value to extract from the body
     */
    fun field(extractValue: (T) -> String, init: FieldContextDslInit<T>) {
        val fieldContext = FieldContextDsl(extractValue).apply(init)
        fields.add(fieldContext)
    }
}

class FieldContextDsl<T>(val extractValue: (T) -> String, val validators: ArrayList<ValidationInfo> = arrayListOf()) {

    /**
     * Adds a validator to the field context
     */
    operator fun ValidatorType.unaryPlus() {
        validators.add(ValidationInfo(this, Allowed(true)))
    }

    /**
     * Adds a disallowed validator to the field context
     */
    operator fun ValidatorType.unaryMinus() {
        validators.add(ValidationInfo(this, Allowed(false)))
    }
}