package org.melvdlin.chat_app_kt.chatplugin.client

import javafx.application.Platform
import org.melvdlin.chat_app_kt.chatplugin.client.view.ui.*
import org.melvdlin.chat_app_kt.chatplugin.traffic.client.requests.LoginRequest
import org.melvdlin.chat_app_kt.chatplugin.traffic.server.MessageBroadcast
import org.melvdlin.chat_app_kt.core.ConnectionHandler
import org.melvdlin.chat_app_kt.core.IncomingTrafficHandler
import org.melvdlin.chat_app_kt.core.client.Client
import org.melvdlin.chat_app_kt.core.traffic.ErrorNotification
import org.melvdlin.chat_app_kt.core.traffic.Traffic
import org.melvdlin.chat_app_kt.core.traffic.server.ServerTraffic
import org.melvdlin.chat_app_kt.core.traffic.server.responses.OkResponse
import java.util.concurrent.SynchronousQueue

class Controller(
    private val connectionHandler : ConnectionHandler,
    private val incomingTrafficHandler : IncomingTrafficHandler,
    private val model : Model
    ) : Thread() {

    private val stageManager = StageManager()
    private val messageQueue : SynchronousQueue<Any> = SynchronousQueue()

    override fun run() {
        incomingTrafficHandler.addOnTrafficReceivedListener(this::onTrafficReceived)

        lateinit var loginUI : LoginUI
        lateinit var fetchingUI : FetchingUI
        lateinit var chatUI : ChatUI

        Platform.runLater {
            loginUI = LoginUI(this, model)
            fetchingUI = FetchingUI()
            chatUI = ChatUI(this, model)
            stageManager.addAll(loginUI, fetchingUI, chatUI)

            messageQueue.offer(Any())
        }

        try {
            messageQueue.take()

            Platform.runLater(loginUI::show)
            messageQueue.take()

            Platform.runLater(fetchingUI::show)
            messageQueue.take()

            Platform.runLater(chatUI::show)
            messageQueue.take()

        } catch (_ : InterruptedException) {
            interrupt()
        } finally {
            synchronized(model) {
                model.appState = AppState.TERMINATED
            }
        }
    }

    fun onConnectionClosing() {
        stageManager.closeStages()
    }

    fun exit() {
        connectionHandler.close()
    }

    fun login(username : String, onFailed : () -> Unit = { }) {
        connectionHandler.sendTimeoutRequest(
            LoginRequest(username),
            Client.Constants.timeoutMillis,
            {
                if (it !is OkResponse || !messageQueue.offer(Any())) {
                    onFailed()
                }
            },
            onFailed
        )
    }

    fun sendMessage(body : String, onFailed : () -> Unit = { }) {

    }

    private fun onTrafficReceived(traffic : Traffic) {
        if (traffic !is ServerTraffic) return

        when (traffic) {
            is ErrorNotification -> {
                synchronized(model) {
                    model.appState = AppState.ERROR
                }
                val errorPopup = ErrorPopup(traffic.fatal, traffic.info)
                stageManager.add(errorPopup)
                Platform.runLater(errorPopup::show)
                exit()
            }

            is MessageBroadcast -> {
                synchronized(model) {
                    if (model.appState == AppState.CHATTING) {
                        model.messageLog += traffic.msg
                    }
                }
            }
        }
    }
}