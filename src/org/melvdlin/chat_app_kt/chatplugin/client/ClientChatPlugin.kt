package org.melvdlin.chat_app_kt.chatplugin.client

import org.melvdlin.chat_app_kt.core.plugin.ClientPlugin
import org.melvdlin.chat_app_kt.core.deprecated.ConnectionHandler
import org.melvdlin.chat_app_kt.core.deprecated.IncomingTrafficHandler

class ClientChatPlugin : ClientPlugin {

    private val model = Model()
    private var connectionHandler : ConnectionHandler? = null
    private var incomingTrafficHandler : IncomingTrafficHandler? = null

    private val controllerLock = Any()
    private var controller : Controller? = null

    override fun onClientStartup() { }

    override fun onConnectionEstablished(
        connectionHandler : ConnectionHandler,
        incomingTrafficHandler : IncomingTrafficHandler,
    ) {
        this.connectionHandler = connectionHandler
        this.incomingTrafficHandler = incomingTrafficHandler

        synchronized(controllerLock) {
            controller = Controller(connectionHandler, incomingTrafficHandler, model)
            controller!!.start()
        }
    }

    override fun onConnectionClosing() {
        synchronized(controllerLock) {
            controller?.onConnectionClosing()
        }
    }
}