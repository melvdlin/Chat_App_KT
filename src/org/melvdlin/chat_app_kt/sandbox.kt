@file:Suppress("UNREACHABLE_CODE", "UNUSED", "UNUSED_VARIABLE")

package org.melvdlin.chat_app_kt

import java.util.*
import kotlin.concurrent.schedule
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

class SomeClass {
    fun someFun() {
        val t = Timer()

        t.schedule(5000) {
            println("ayyy lmao")
        }
        t.schedule(5001) {
            t.cancel()
        }
    }
}


fun main() {

    SomeClass().someFun()

    Thread.sleep(6000)
    println("these nuts")

//    try {
//        synchronized(Any()) {
//            throw Exception()
//        }
//    } catch (e : NullPointerException) {
//        println("exception!")
//    } finally {
//        println("finally...")
//    }

//    val someSand = BoxSand()

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