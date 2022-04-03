package org.melvdlin.chat_app_kt.core.plugin

import org.melvdlin.chat_app_kt.core.ConnectionHandler
import org.melvdlin.chat_app_kt.core.IncomingTrafficHandler

interface Plugin {
    fun onConnectionEstablished(connectionHandler : ConnectionHandler, incomingTrafficHandler : IncomingTrafficHandler)
    fun onConnectionClosing()
}