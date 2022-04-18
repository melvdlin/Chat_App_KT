package org.melvdlin.chat_app_kt.core.netcode.impl

import org.melvdlin.chat_app_kt.core.netcode.ConnectionHandler
import org.melvdlin.chat_app_kt.core.plugin.Plugin
import org.melvdlin.chat_app_kt.core.traffic.Request
import org.melvdlin.chat_app_kt.core.traffic.Response
import org.melvdlin.chat_app_kt.core.traffic.Traffic
import java.net.Socket

internal class DefaultConnectionHandler(private val socket : Socket, private val plugins : Collection<Plugin>) : ConnectionHandler {

    private val inHandler = IncomingTrafficHandler(socket.getInputStream()!!, ::onInHandlerClosed, ::onInHandlerError)
    private val outHandler = OutgoingTrafficHandler(socket.getOutputStream()!!, ::onOutHandlerClosed, ::onOutHandlerError)

    private val onClosedListeners = HashSet<() -> Unit>()

    override fun start() {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    override fun addOnClosedListener(listener : () -> Unit) : Boolean {
        TODO("Not yet implemented")
    }

    override fun removeOnClosedListener(listener : () -> Unit) : Boolean {
        TODO("Not yet implemented")
    }

    override fun addOnTrafficReceivedListener(listener : (Traffic) -> Unit) : Boolean {
        return inHandler.addOnTrafficReceivedListener(listener)
    }

    override fun removeOnTrafficReceivedListener(listener : (Traffic) -> Unit) : Boolean {
        return inHandler.removeOnTrafficReceivedListener(listener)
    }

    override fun sendTraffic(traffic : Traffic) {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    private fun onOutHandlerClosed() {
        TODO("Not yet implemented")
    }

    private fun onInHandlerError() {
        TODO("Not yet implemented")
    }

    private fun onOutHandlerError() {
        TODO("Not yet implemented")
    }
}
