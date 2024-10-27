package jetzt.arbeitszeitbetrug.app.util

import io.ktor.http.*
import kotlin.test.Test

class ValidatorTest {

    data class TestObject(val test: String)

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
        val testObject = OtherTestObject("abc", "def")
        val parameterValidatorResult = bodyValidator.validate(Parameters.Empty, testObject)
        assert(parameterValidatorResult is ValidatorResult.Error)
    }
}