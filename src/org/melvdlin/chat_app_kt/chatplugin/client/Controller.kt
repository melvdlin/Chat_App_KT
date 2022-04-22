package org.melvdlin.chat_app_kt.chatplugin.client

import javafx.application.Platform
import org.melvdlin.chat_app_kt.chatplugin.client.view.ui.*
import org.melvdlin.chat_app_kt.chatplugin.traffic.client.requests.DisconnectRequest
import org.melvdlin.chat_app_kt.chatplugin.traffic.client.requests.FetchMessageLogRequest
import org.melvdlin.chat_app_kt.chatplugin.traffic.client.requests.LoginRequest
import org.melvdlin.chat_app_kt.chatplugin.traffic.client.requests.SendMessageRequest
import org.melvdlin.chat_app_kt.chatplugin.traffic.server.MessageBroadcast
import org.melvdlin.chat_app_kt.chatplugin.traffic.server.responses.FetchMessageLogResponse
import org.melvdlin.chat_app_kt.core.netcode.ConnectionHandler
import org.melvdlin.chat_app_kt.core.client.Client
import org.melvdlin.chat_app_kt.core.traffic.ErrorNotification
import org.melvdlin.chat_app_kt.core.traffic.Response
import org.melvdlin.chat_app_kt.core.traffic.Traffic
import org.melvdlin.chat_app_kt.core.traffic.server.ServerTraffic
import org.melvdlin.chat_app_kt.core.traffic.server.responses.OkResponse
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.TimeUnit

class Controller(
    private val connectionHandler : ConnectionHandler,
    private val model : Model
    ) : Thread() {

    private val stageManager = StageManager()
    private val messageQueue : SynchronousQueue<Any> = SynchronousQueue()

    override fun run() {
        connectionHandler.addOnTrafficReceivedListener(this::onTrafficReceived)

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

            // LOGIN

            synchronized(model) { model.appState = AppState.LOGIN }
            Platform.runLater(loginUI::show)
            messageQueue.take()

            // FETCH_LOG

            synchronized(model) { model.appState = AppState.FETCH_LOG }
            Platform.runLater(loginUI::hide)
            Platform.runLater(fetchingUI::show)

            val fetchMessageLogFailHandler = {
                Platform.runLater {
                    val ep = ErrorPopup(false, "Failed to fetch message log.")
                    stageManager.add(ep)
                    ep.show()
                }
            }

            val fetchMessageLogResponseHandler : (Response) -> Unit = {
                if (it is FetchMessageLogResponse) {
                    synchronized(model) {
                        model.messageLog.addAll(it.messageLog)
                    }
                } else {
                    fetchMessageLogFailHandler()
                }
            }

            connectionHandler.sendTimeoutRequest(
                FetchMessageLogRequest(Client.Constants.backlog),
                Client.Constants.timeoutMillis,
                {
                    fetchMessageLogFailHandler()
                    @Suppress("BlockingMethodInNonBlockingContext")
                    messageQueue.offer(Any(), 1000, TimeUnit.MILLISECONDS)
                }, {
                    fetchMessageLogResponseHandler(it)
                    @Suppress("BlockingMethodInNonBlockingContext")
                    messageQueue.offer(Any(), 1000, TimeUnit.MILLISECONDS)
                }
            )

            messageQueue.take()

            // CHATTING

            synchronized(model) { model.appState = AppState.CHATTING   }
            Platform.runLater(fetchingUI::hide)
            Platform.runLater(chatUI::show)
            Platform.runLater(chatUI.messageEntryBox.textField::requestFocus)
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
        TODO("Fix this, there is zero structure in this method:" +
                "if called while in LOGIN state, just advances app flow," +
                "sends disconnect req and closes connection handler," +
                "app flow then proceeds to sends fetch log req resulting" +
                "in illegal state exception from connection handler due to" +
                "connection handler being closed")
        synchronized(model) {
            if (model.appState in listOf(AppState.LOGIN, AppState.FETCH_LOG, AppState.CHATTING)) {
                connectionHandler.sendTraffic(DisconnectRequest())
            }
        }
        messageQueue.offer(Any())
        connectionHandler.close()
    }

    fun login(username : String, onFailed : () -> Unit = { }) {
        connectionHandler.sendTimeoutRequest(
            LoginRequest(username),
            Client.Constants.timeoutMillis,
            onFailed,
        ) {
            if (it !is OkResponse || !messageQueue.offer(Any())) {
                onFailed()
            }
        }
    }

    fun sendMessage(body : String, onFailed : () -> Unit = { }) {
        connectionHandler.sendTimeoutRequest(
            SendMessageRequest(body),
            Client.Constants.timeoutMillis,
            onFailed,
        ) {
            if (it !is OkResponse) {
                onFailed()
            }
        }
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