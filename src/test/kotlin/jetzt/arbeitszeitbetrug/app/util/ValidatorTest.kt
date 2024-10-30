package jetzt.arbeitszeitbetrug.app.util

import io.ktor.http.*
import kotlin.test.Test

class ValidatorTest {

    private var parameters: Parameters

    init {
        val testParams = ParametersBuilder()
        testParams.append("test", "123")

        parameters = testParams.build()
    }

    @Test
    fun testEmptyValidator() {
        val emptyValidator = Validator.None

        val emptyValidatorResult = emptyValidator.validate(Parameters.Empty, Unit)
        assert(emptyValidatorResult is ValidatorResult.Ok)
    }

    data class TestObject(val test: String)

    @Test
    fun testParameterValidator() {
        val paramValidator = validatorChain {
            param("test") {
                +Validators.notEmpty()
                +Validators.isInt()
                +Validators.length(0, 3)
            }
        }
        val parameterValidatorResult = paramValidator.validate(parameters, Unit)
        assert(parameterValidatorResult is ValidatorResult.Ok)

        val parameterErrorValidator = validatorChain {
            param("test") {
                -Validators.isInt()
            }
        }
        val parameterErrorValidatorResult = parameterErrorValidator.validate(parameters, Unit)
        assert(parameterErrorValidatorResult is ValidatorResult.Error)
    }

    @Test
    fun testBodyValidator() {
        val bodyValidator = validatorChain<TestObject> {
            body {
                field({ it.test }) {
                    +Validators.isInt()
                }
            }
        }
        val testObject = TestObject("123")
        val parameterValidatorResult = bodyValidator.validate(Parameters.Empty, testObject)
        assert(parameterValidatorResult is ValidatorResult.Ok)

        val bodyErrorValidator = validatorChain<TestObject> {
            body {
                field({ it.test }) {
                    -Validators.isInt()
                }
            }
        }
        val parameterErrorValidatorResult = bodyErrorValidator.validate(Parameters.Empty, testObject)
        assert(parameterErrorValidatorResult is ValidatorResult.Error)
    }

    data class OtherTestObject(val otherTest: String, val otherTest2: String)

    @Test
    fun testInvalidBody() {
        //We except body to be the structure of TestObject
        val bodyValidator = validatorChain<TestObject> {
            body {
                field({ it.test }) {
                    +Validators.isInt()
                }
            }
        }
        //But instead the call contains the structure of OtherTestObject
        val testObject = OtherTestObject("abc", "def")
        val parameterValidatorResult = bodyValidator.validate(Parameters.Empty, testObject)
        assert(parameterValidatorResult is ValidatorResult.Error)
    }
}