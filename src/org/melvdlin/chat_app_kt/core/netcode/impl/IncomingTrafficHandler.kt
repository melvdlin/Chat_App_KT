package org.melvdlin.chat_app_kt.core.netcode.impl

import org.melvdlin.chat_app_kt.core.traffic.Traffic
import java.io.InputStream
import java.io.ObjectInputStream

internal class IncomingTrafficHandler(
    private val stream : InputStream,
    private val onClosed : () -> Unit,
    private val onError : () -> Unit
) : AutoCloseable {

    private val listeners : MutableCollection<(Traffic) -> Unit> = mutableSetOf()

    private val worker = Thread(::work, "IncomingTrafficWorker")

    var state : HandlerState = HandlerState.UNINITIALIZED
        private set

    private fun work() {
        ObjectInputStream(stream).use {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    val traffic = it.readObject() as Traffic
                    listeners.forEach {
                        it(traffic)
                    }
                } catch (_ : InterruptedException) {

                } catch (_ : Throwable) {
                    synchronized(state) {
                        state = HandlerState.ERROR
                        onError()
                    }
                }
            }
        }

        if (synchronized(state) { (state == HandlerState.CLOSING).also { if (it) state = HandlerState.CLOSED } }) {
            onClosed()
        }
    }

    fun start() {
        synchronized(state) {
            state = HandlerState.RUNNING
            worker.start()
        }
    }

    override fun close() {
        synchronized(state) {
            if (state >= HandlerState.CLOSING)
                return
            state = HandlerState.CLOSING
        }
        worker.interrupt()
    }

    fun addOnTrafficReceivedListener(listener : (Traffic) -> Unit) = synchronized(listeners) { listeners.add(listener) }
    fun removeOnTrafficReceivedListener(listener : (Traffic) -> Unit) = synchronized(listeners) { listeners.remove(listener) }
}