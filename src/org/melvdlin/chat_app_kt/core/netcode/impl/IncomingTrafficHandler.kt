package org.melvdlin.chat_app_kt.core.netcode.impl

import org.melvdlin.chat_app_kt.core.traffic.Traffic
import java.io.InputStream
import java.io.ObjectInputStream

internal class IncomingTrafficHandler(
    private val stream : InputStream,
    private val onDisconnectRequested : (DisconnectRequest) -> Unit,
    private val onClosed : () -> Unit,
    private val onError : () -> Unit
) : AutoCloseable {
    companion object Constants {
        private val illegalStates = arrayOf(
            HandlerState.CLOSING,
            HandlerState.CLOSED,
            HandlerState.ERROR
        )
    }

    var state : HandlerState = HandlerState.UNINITIALIZED
    private set

    private val listeners : MutableList<(Traffic) -> Unit>
    = mutableListOf({
        if (it is DisconnectRequest) {
            onDisconnectRequested(it)
        }
    })

    private val worker = Thread(::work)

    fun start() {
        worker.start()
        synchronized(state) {
            state = HandlerState.RUNNING
        }
    }

    private fun work() {
        ObjectInputStream(stream).use {
            var traffic : Traffic
            while(!Thread.currentThread().isInterrupted
                && synchronized(state){state}
                == HandlerState.RUNNING
            ) {
                try {
                    traffic = it.readObject() as Traffic
                    synchronized(listeners) {
                        listeners.forEach { listener ->
                            listener(traffic)
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
        synchronized(state) {
            if (state != HandlerState.ERROR) {
                state = HandlerState.CLOSED
                onClosed()
            }
        }
    }

    fun kill() {
        worker.interrupt()
    }

    override fun close() {
        synchronized(state) {
            state.ensureIsNoneOf(*illegalStates)
            state = HandlerState.CLOSING
        }
        worker.interrupt()
    }

    fun addOnTrafficReceivedListener(listener : (Traffic) -> Unit) = synchronized(listeners) { listeners.add(listener) }
    fun removeOnTrafficReceivedListener(listener : (Traffic) -> Unit) = synchronized(listeners) { listeners.remove(listener) }
}