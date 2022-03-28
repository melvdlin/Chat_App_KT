package org.melvdlin.chat_app_kt.plugins

import org.melvdlin.chat_app_kt.util.ConnectionHandler
import org.melvdlin.chat_app_kt.util.IncomingTrafficHandler

interface Plugin {
    fun onConnectionEstablished(connectionHandler : ConnectionHandler, incomingTrafficHandler : IncomingTrafficHandler)
    fun onConnectionClosing()
}