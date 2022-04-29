package org.melvdlin.chat_app_kt.core.plugin

import org.melvdlin.chat_app_kt.core.netcode.ConnectionHandler

interface Plugin {
    fun onConnectionEstablished(connectionHandler : ConnectionHandler)

    //TODO("Decide on whether to keep the following two OR the add/remove On Closed/Error Listener
    //      methods in the ConnectionHandler interface")
    fun onConnectionClosed()
    fun onConnectionError()
}