package org.melvdlin.chat_app_kt.plugins.server

import org.melvdlin.chat_app_kt.server.ConnectionHandler
import org.melvdlin.chat_app_kt.server.IncomingTrafficHandler

interface ServerPlugin {
    fun onServerStartup()
    fun onConnectionAccepted(connectionHandler : ConnectionHandler, incomingTrafficHandler : IncomingTrafficHandler)
}