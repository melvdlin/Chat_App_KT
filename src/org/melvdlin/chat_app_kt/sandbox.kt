@file:Suppress("UNREACHABLE_CODE", "UNUSED", "UNUSED_VARIABLE")

package org.melvdlin.chat_app_kt

import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

fun main() {
    val someSand = BoxSand()

//    println(someSand::lazy.isLazyInitialized)
}

class BoxSand {
    val lazy : String by lazy {
        "ayy"
    }
}

val KProperty0<*>.isLazyInitialized : Boolean
get() {
    val originalAccessLevel = isAccessible
    isAccessible = true
    val retVal = (getDelegate() as Lazy<*>).isInitialized()
    isAccessible = originalAccessLevel
    return retVal
}