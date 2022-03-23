package org.melvdlin.chat_app_kt.testbed

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
}

val lock = ReentrantLock()

fun somefun() : Int {
    synchronized(lock) {
        return 5
    }
}