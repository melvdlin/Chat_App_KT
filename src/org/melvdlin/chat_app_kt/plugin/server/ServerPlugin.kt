package org.melvdlin.chat_app_kt.plugin.server

import org.melvdlin.chat_app_kt.server.ConnectionHandler
import org.melvdlin.chat_app_kt.server.IncomingTrafficHandler
import org.melvdlin.chat_app_kt.server.MessageLog

interface ServerPlugin {
    fun onConnectionAccepted(messageLog : MessageLog, connectionHandler : ConnectionHandler, incomingTrafficHandler : IncomingTrafficHandler)
}