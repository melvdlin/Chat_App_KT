package org.melvdlin.chat_app_kt.core.netcode.impl

import org.melvdlin.chat_app_kt.core.traffic.Traffic
import java.io.InputStream

internal class IncomingTrafficHandler(
    private val stream : InputStream,
    private val onClosed : () -> Unit,
    private val onError : () -> Unit
) : AutoCloseable {

    val state : HandlerState
    get() {
        TODO("Not yet implemented")
    }

    private val listeners : MutableCollection<(Traffic) -> Unit> = HashSet()

    private val worker = Thread(::work)

    fun start() {
        TODO("Not yet implemented")
    }

    private fun work() {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    fun addOnTrafficReceivedListener(listener : (Traffic) -> Unit) = listeners.add(listener)
    fun removeOnTrafficReceivedListener(listener : (Traffic) -> Unit) = listeners.remove(listener)
}