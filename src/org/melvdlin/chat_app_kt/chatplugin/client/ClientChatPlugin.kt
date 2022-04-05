package org.melvdlin.chat_app_kt.chatplugin.client

import javafx.application.Platform
import javafx.stage.Stage
import org.melvdlin.chat_app_kt.chatplugin.client.view.ui.LoginUI
import org.melvdlin.chat_app_kt.chatplugin.client.view.ui.StageManager
import org.melvdlin.chat_app_kt.core.plugin.ClientPlugin
import org.melvdlin.chat_app_kt.core.ConnectionHandler
import org.melvdlin.chat_app_kt.core.IncomingTrafficHandler

class ClientChatPlugin : ClientPlugin {

    private val model = Model()
    private val stageManager = StageManager()
    private var connectionHandler : ConnectionHandler? = null
    private var incomingTrafficHandler : IncomingTrafficHandler? = null

    override fun onClientStartup() { }

    override fun onConnectionEstablished(
        connectionHandler : ConnectionHandler,
        incomingTrafficHandler : IncomingTrafficHandler,
    ) {
        this.connectionHandler = connectionHandler
        this.incomingTrafficHandler = incomingTrafficHandler

        Platform.runLater {
            val loginUI = LoginUI(this, model)
            stageManager.add(loginUI)
            loginUI.show()
        }

    }

    override fun onConnectionClosing() {
        stageManager.closeStages()
    }

    fun exit() {
        connectionHandler?.close()
    }

    fun login(username : String, onFailed : () -> Unit = { }) {

    }

    fun sendMessage(body : String, onFailed : () -> Unit = { }) {

    }
}