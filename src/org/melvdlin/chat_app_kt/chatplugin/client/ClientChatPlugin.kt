package org.melvdlin.chat_app_kt.chatplugin.client

import org.melvdlin.chat_app_kt.core.plugin.ClientPlugin
import org.melvdlin.chat_app_kt.core.ConnectionHandler
import org.melvdlin.chat_app_kt.core.IncomingTrafficHandler

class ClientChatPlugin : ClientPlugin {

    private val model = Model()

    override fun onClientStartup() { }

    override fun onConnectionEstablished(
        connectionHandler : ConnectionHandler,
        incomingTrafficHandler : IncomingTrafficHandler,
    ) {

    }

    override fun onConnectionClosing() { }
}