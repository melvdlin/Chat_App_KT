@file:Suppress("UNREACHABLE_CODE", "UNUSED", "UNUSED_VARIABLE")

package org.melvdlin.chat_app_kt

import java.io.EOFException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

abstract class SomeAbstract()
data class SomeData(val l : Long, val s : String, val d : Double) : SomeAbstract()

fun main() {

    val m = mutableMapOf<SomeData, Int>()

    val obj1 = SomeData(1, "deez", 4.2)
    val obj2 = SomeData(1, "deez", 4.2)

    m += obj1 to 1

    println(m.remove(obj2))

}