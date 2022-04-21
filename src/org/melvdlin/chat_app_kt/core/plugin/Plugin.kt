package org.melvdlin.chat_app_kt.core.plugin

import org.melvdlin.chat_app_kt.core.netcode.ConnectionHandler

interface Plugin {
    fun onConnectionEstablished(connectionHandler : ConnectionHandler)
    fun onConnectionClosed()
    fun onConnectionError()
}