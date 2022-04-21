package org.melvdlin.chat_app_kt.core.netcode.impl

import org.melvdlin.chat_app_kt.core.netcode.ConnectionHandler
import org.melvdlin.chat_app_kt.core.plugin.Plugin
import org.melvdlin.chat_app_kt.core.traffic.Request
import org.melvdlin.chat_app_kt.core.traffic.Response
import org.melvdlin.chat_app_kt.core.traffic.Traffic
import java.net.Socket

internal class DefaultConnectionHandler(private val socket : Socket, private val plugins : Collection<Plugin>) : ConnectionHandler {

    private val outHandler =
        OutgoingTrafficHandler(
            socket.getOutputStream()!!,
            ::onOutHandlerClosed,
            ::onOutHandlerError
        )

    private val inHandler =
        IncomingTrafficHandler(
            socket.getInputStream()!!,
            {
                try { outHandler.terminate(it) }
                catch (_ : IllegalStateException) { }
            },
            ::onInHandlerClosed,
            ::onInHandlerError
        )

    private val onClosedListeners = HashSet<() -> Unit>()
    private val onErrorListeners = HashSet<() -> Unit>()

    override fun start() {
        plugins.forEach { it.onConnectionEstablished(this) }
        inHandler.start()
        outHandler.start()
    }

    override fun close() {
        outHandler.close()
    }

    override fun addOnClosedListener(listener : () -> Unit) : Boolean {
        return synchronized(onClosedListeners) { onClosedListeners.add(listener) }
    }

    override fun removeOnClosedListener(listener : () -> Unit) : Boolean {
        return synchronized(onClosedListeners) { onClosedListeners.remove(listener) }
    }

    override fun addOnErrorListener(listener : () -> Unit) : Boolean {
        return synchronized(onErrorListeners) { onErrorListeners.add(listener) }
    }

    override fun removeOnErrorListener(listener : () -> Unit) : Boolean {
        return synchronized(onErrorListeners) { onErrorListeners.remove(listener) }
    }

    override fun addOnTrafficReceivedListener(listener : (Traffic) -> Unit) : Boolean {
        return inHandler.addOnTrafficReceivedListener(listener)
    }

    override fun removeOnTrafficReceivedListener(listener : (Traffic) -> Unit) : Boolean {
        return inHandler.removeOnTrafficReceivedListener(listener)
    }

    override fun sendTraffic(traffic : Traffic) {

    }

    override fun sendTimeoutRequest(
        request : Request,
        timeoutMillis : Long,
        onTimeout : () -> Unit,
        responseHandler : (Response) -> Unit,
    ) {
        outHandler.sendTimeoutRequest(
            request,
            timeoutMillis,
            onTimeout,
            responseHandler
        )
    }

    private fun onInHandlerClosed() {
        if (synchronized(outHandler.state) {
                outHandler.state != HandlerState.CLOSED
            }){
            outHandler.kill()
        }
        try {
            socket.close()
        } catch (_ : Throwable) { }
        onClosedListeners.forEach{ it() }
    }

    private fun onOutHandlerClosed() {
        if (synchronized(inHandler.state) {
            inHandler.state == HandlerState.CLOSED
        }) {
            try {
                socket.close()
            } catch (_ : Throwable) { }
            onClosedListeners.forEach{ it() }
        } else {
            inHandler.close()
        }
    }

    private fun onInHandlerError() {
        outHandler.kill()
        socket.close()
        synchronized(onErrorListeners) {
            onErrorListeners.forEach {
                it()
            }
        }
    }

    private fun onOutHandlerError() {
        inHandler.kill()
        socket.close()
        synchronized(onErrorListeners) {
            onErrorListeners.forEach {
                it()
            }
        }
    }
}
