package jetzt.arbeitszeitbetrug.app.util

object Reflections {

    /**
     * Checks if the given object is of the same type as the other object.^
     * @param obj The object to check
     * @param other The other object to check against
     * @return True if the objects are of the same type, false otherwise
     */
    fun isTypeOf(obj: Any, other: Any): Boolean {
        return other::class.java.isInstance(obj)
    }
}