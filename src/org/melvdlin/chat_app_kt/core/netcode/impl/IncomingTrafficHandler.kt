package org.melvdlin.chat_app_kt.core.netcode.impl

import org.melvdlin.chat_app_kt.core.traffic.Traffic
import java.io.EOFException
import java.io.InputStream
import java.io.ObjectInputStream
import java.net.SocketException

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
                } catch (_ : EOFException) {
                    synchronized(state) {
                        if (state == HandlerState.CLOSING)
                            Thread.currentThread().interrupt()
                        else {
                            state = HandlerState.ERROR
                            onError()
                        }
                    }
                } catch (_ : SocketException) {
                    synchronized(state) {
                        if (state == HandlerState.CLOSING)
                            Thread.currentThread().interrupt()
                        else {
                            state = HandlerState.ERROR
                            onError()
                        }
                    }
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
    }

    fun addOnTrafficReceivedListener(listener : (Traffic) -> Unit) = synchronized(listeners) { listeners.add(listener) }
    fun removeOnTrafficReceivedListener(listener : (Traffic) -> Unit) = synchronized(listeners) { listeners.remove(listener) }
}