package org.melvdlin.chat_app_kt.testbed


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
    SomeClass(AnotherClass()).someFun()
}