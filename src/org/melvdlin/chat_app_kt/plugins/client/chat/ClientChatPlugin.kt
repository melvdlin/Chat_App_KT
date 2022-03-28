package org.melvdlin.chat_app_kt.plugins.client.chat

import javafx.scene.Scene
import org.melvdlin.chat_app_kt.plugins.client.ClientPlugin
import org.melvdlin.chat_app_kt.util.ConnectionHandler
import org.melvdlin.chat_app_kt.util.IncomingTrafficHandler
import java.net.Socket

class ClientChatPlugin : ClientPlugin {

    override fun onClientStartup() { }

    override fun onConnectionEstablished(
        connectionHandler : ConnectionHandler,
        incomingTrafficHandler : IncomingTrafficHandler,
    ) {
        TODO("Not yet implemented")
    }

    override fun onConnectionClosing() { }
}