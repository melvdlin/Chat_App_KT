@file:Suppress("UNREACHABLE_CODE")

package org.melvdlin.chat_app_kt

import java.util.concurrent.locks.ReentrantLock

val walker : StackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)

class SomeClass(private val anotherInstance : AnotherClass) {
    fun someFun() {
        println(anotherInstance.anotherFun())
    }
}

class AnotherClass {
    fun anotherFun() : String = walker.callerClass.simpleName
}

fun main() {
    println(somefun())
    SomeClass(AnotherClass()).someFun()
    val o = Any()
    anotherfun(15, o)
    anotherfun(25, o)
}

val lock = ReentrantLock()

val someString : String by lazy { return@lazy "deez" }

fun somefun() : Int {
    synchronized(lock) {
        return 5
    }
}

fun anotherfun(i : Int, o : Any) {
    synchronized(o) {
        return
    }
    println(i)
}