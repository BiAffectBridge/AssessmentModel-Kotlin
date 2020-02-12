package org.sagebionetworks.assessmentmodel

import android.os.Build
import java.time.Instant
import java.util.*
import kotlin.reflect.KClass

actual class Platform actual constructor() {
    actual val platform: String = "Android"
}

actual class Product(actual val user: String) {
    fun androidSpecificOperation() {
        println("I am ${Build.MODEL} by ${Build.MANUFACTURER}")
    }

    override fun toString() = "Android product of $user for ${Build.MODEL}"
}

actual object Factory {
    actual fun create(config: Map<String, String>) =
        Product(config["user"]!!)

    actual val platform: String = "android"
}

actual object UUIDGenerator {
    actual fun uuidString() : String = UUID.randomUUID().toString()
}

actual object DateGenerator {
    actual fun nowString(): String = "TODO: Implement"
}

actual fun <T : Any> KClass<T>.klassName(): String? = this.simpleName