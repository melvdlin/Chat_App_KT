package org.melvdlin.chat_app_kt.core.netcode

import org.melvdlin.chat_app_kt.core.traffic.*

interface ConnectionHandler : AutoCloseable {
    fun start()
    fun addOnClosedListener(listener : () -> Unit) : Boolean
    fun removeOnClosedListener(listener : () -> Unit) : Boolean
    fun addOnErrorListener(listener : () -> Unit) : Boolean
    fun removeOnErrorListener(listener : () -> Unit) : Boolean
    fun addOnTrafficReceivedListener(listener : (Traffic) -> Unit) : Boolean
    fun removeOnTrafficReceivedListener(listener : (Traffic) -> Unit) : Boolean
    fun sendTraffic(traffic : Traffic)
    fun sendTimeoutRequest(request : Request, timeoutMillis : Long, onTimeout : () -> Unit, responseHandler : (Response) -> Unit)
}