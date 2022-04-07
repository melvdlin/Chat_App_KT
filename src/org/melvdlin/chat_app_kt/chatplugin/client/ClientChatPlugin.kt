package org.melvdlin.chat_app_kt.chatplugin.client

import javafx.application.Platform
import org.melvdlin.chat_app_kt.chatplugin.client.view.ui.*
import org.melvdlin.chat_app_kt.chatplugin.traffic.client.requests.LoginRequest
import org.melvdlin.chat_app_kt.chatplugin.traffic.server.MessageBroadcast
import org.melvdlin.chat_app_kt.core.plugin.ClientPlugin
import org.melvdlin.chat_app_kt.core.ConnectionHandler
import org.melvdlin.chat_app_kt.core.IncomingTrafficHandler
import org.melvdlin.chat_app_kt.core.client.Client
import org.melvdlin.chat_app_kt.core.traffic.ErrorNotification
import org.melvdlin.chat_app_kt.core.traffic.Traffic
import org.melvdlin.chat_app_kt.core.traffic.server.ServerTraffic
import org.melvdlin.chat_app_kt.core.traffic.server.responses.OkResponse
import java.util.concurrent.SynchronousQueue

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