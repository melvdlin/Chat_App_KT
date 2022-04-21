package org.melvdlin.chat_app_kt.chatplugin.client

import org.melvdlin.chat_app_kt.core.plugin.ClientPlugin
import org.melvdlin.chat_app_kt.core.netcode.ConnectionHandler

class ClientChatPlugin : ClientPlugin {

    private val model = Model()
    private var connectionHandler : ConnectionHandler? = null

    private val controllerLock = Any()
    private var controller : Controller? = null

    override fun onClientStartup() { }

    override fun onConnectionEstablished(
        connectionHandler : ConnectionHandler,
    ) {
        this.connectionHandler = connectionHandler

        synchronized(controllerLock) {
            controller = Controller(connectionHandler, model)
            controller!!.start()
        }
    }

    override fun onConnectionClosed() {
        synchronized(controllerLock) {
            controller?.onConnectionClosing()
        }
    }

    override fun onConnectionError() = Unit
}