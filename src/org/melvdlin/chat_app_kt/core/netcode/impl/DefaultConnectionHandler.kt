package org.melvdlin.chat_app_kt.core.netcode.impl

import org.melvdlin.chat_app_kt.core.netcode.ConnectionHandler
import org.melvdlin.chat_app_kt.core.plugin.Plugin
import org.melvdlin.chat_app_kt.core.traffic.*
import org.melvdlin.chat_app_kt.core.traffic.server.responses.OkResponse
import java.net.Socket
import java.util.concurrent.SynchronousQueue

internal class DefaultConnectionHandler(private val socket : Socket, private val plugins : Collection<Plugin>) : ConnectionHandler {

    companion object Constants {
        const val DisconnectRequestTimeout : Long = 3000
    }

    private val outHandler =
        OutgoingTrafficHandler(
            socket.getOutputStream()!!,
            ::onOutHandlerClosed,
            ::onOutHandlerError
        )

    private val outHandlerClosedQueue = SynchronousQueue<Any>()

    private val inHandler =
        IncomingTrafficHandler(
            socket.getInputStream()!!,
            ::onInHandlerClosed,
            ::onInHandlerError
        )

    private val onTrafficReceivedListeners : MutableCollection<(Traffic) -> Unit> = mutableSetOf()

    private val onClosedListeners : MutableCollection<() -> Unit> = mutableSetOf()
    private var onClosedListenersCalled = false
    private val onErrorListeners : MutableCollection<() -> Unit> = mutableSetOf()
    private var onErrorListenersCalled = false

    private var state = HandlerState.UNINITIALIZED

    init {
        //DEBUG
        println("Instantiating new DefaultConnectionHandler...")
        state = HandlerState.IDLE
    }

    override fun start() {
        synchronized(state) {
            if (state != HandlerState.IDLE)
                return


            plugins.forEach { it.onConnectionEstablished(this) }
            inHandler.addOnTrafficReceivedListener(::onTrafficReceived)
            inHandler.start()
            outHandler.start()

            state = HandlerState.RUNNING
        }
    }

    override fun close() {
        synchronized(state) {
            if (state >= HandlerState.CLOSING)
                return
            state = HandlerState.CLOSING
        }

        outHandler.sendTimeoutRequest(
            request = DisconnectRequest(),
            timeoutMillis = DisconnectRequestTimeout,
            onTimeout = ::onClosingNotAcknowledged,
            responseHandler = {
                if (it is OkResponse) {
                    onClosingAcknowledged()
                } else {
                    onClosingNotAcknowledged()
                }
            }
        )
    }

    private fun onTrafficReceived(traffic : Traffic) {
        if (traffic is DisconnectRequest)
            onDisconnectRequestReceived(traffic)
        if (traffic is Response)
            outHandler.onResponseReceived(traffic)
        synchronized(onTrafficReceivedListeners) {
            onTrafficReceivedListeners.forEach { it(traffic) }
        }
    }

    private fun onDisconnectRequestReceived(req : DisconnectRequest) {
        synchronized(state) {
            if (state < HandlerState.CLOSING)
                state = HandlerState.CLOSING
            else if (state > HandlerState.CLOSING)
                return

            outHandler.sendTraffic(
                CallbackTraffic(
                    OkResponse(req),
                    ::onClosingAcknowledged
                )
            )
        }
    }

    private fun onClosingNotAcknowledged() {
        synchronized(state) {
            state = HandlerState.ERROR
        }
        onErrorListeners.forEach { it.invoke() }
        outHandler.close()
        inHandler.close()
        outHandlerClosedQueue.take()
        socket.close()
        onError()
    }

    private fun onClosingAcknowledged() {
        synchronized(state) {
            state = HandlerState.CLOSED
        }
        outHandler.close()
        inHandler.close()
        socket.close()
        onClosed()
    }

    private fun onClosed() {
        synchronized(onClosedListeners) {
            if (!onClosedListenersCalled) {
                onClosedListenersCalled = true
                onClosedListeners.forEach {
                    it()
                }
                plugins.forEach { it.onConnectionClosed() }
            }
        }
    }

    private fun onError() {
        synchronized(onErrorListeners) {
            if (!onErrorListenersCalled) {
                onErrorListenersCalled = true
                onErrorListeners.forEach {
                    it()
                }
                plugins.forEach { it.onConnectionError() }
            }
        }
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
        return synchronized(onTrafficReceivedListeners) { onTrafficReceivedListeners.add(listener) }
    }

    override fun removeOnTrafficReceivedListener(listener : (Traffic) -> Unit) : Boolean {
        return synchronized(onTrafficReceivedListeners) { onTrafficReceivedListeners.remove(listener) }
    }

    override fun sendTraffic(traffic : Traffic) {
        if (synchronized(state) { state } != HandlerState.RUNNING ||
            synchronized(outHandler.state) { outHandler.state } != HandlerState.RUNNING)
            throw IllegalStateException()
        outHandler.sendTraffic(traffic)
    }

    override fun sendTimeoutRequest(
        request : Request,
        timeoutMillis : Long,
        onTimeout : () -> Unit,
        responseHandler : (Response) -> Unit,
    ) {
        if (synchronized(state) { state } != HandlerState.RUNNING ||
            synchronized(outHandler.state) { outHandler.state } != HandlerState.RUNNING ||
            synchronized(inHandler.state) { inHandler.state } != HandlerState.RUNNING)
            throw IllegalStateException()

        outHandler.sendTimeoutRequest(
            request,
            timeoutMillis,
            onTimeout,
            responseHandler
        )
    }

    private fun onInHandlerClosed() { }

    private fun onOutHandlerClosed() { }

    private fun onInHandlerError() {
        synchronized(state) {
            state = HandlerState.ERROR
            outHandler.close()
            socket.close()
            onError()
        }
    }

    private fun onOutHandlerError() {
        synchronized(state) {
            state = HandlerState.ERROR
            inHandler.close()
            socket.close()
            onError()
        }
    }
}
