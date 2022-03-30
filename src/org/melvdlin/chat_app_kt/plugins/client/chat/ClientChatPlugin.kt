package org.melvdlin.chat_app_kt.plugins.client.chat

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.util.Callback
import org.melvdlin.chat_app_kt.plugins.client.ClientPlugin
import org.melvdlin.chat_app_kt.plugins.client.chat.fx.ChatMessageListCell
import org.melvdlin.chat_app_kt.plugins.client.chat.fx.TextEntryBox
import org.melvdlin.chat_app_kt.plugins.server.chat.ChatMessage
import org.melvdlin.chat_app_kt.traffic.ErrorNotification
import org.melvdlin.chat_app_kt.traffic.client.requests.FetchMessageLogRequest
import org.melvdlin.chat_app_kt.traffic.client.requests.LoginRequest
import org.melvdlin.chat_app_kt.traffic.client.requests.SendMessageRequest
import org.melvdlin.chat_app_kt.traffic.server.MessageBroadcast
import org.melvdlin.chat_app_kt.traffic.server.responses.FetchMessageLogResponse
import org.melvdlin.chat_app_kt.traffic.server.responses.OkResponse
import org.melvdlin.chat_app_kt.util.ConnectionHandler
import org.melvdlin.chat_app_kt.util.IncomingTrafficHandler
import java.util.concurrent.locks.ReentrantLock

class ClientChatPlugin : ClientPlugin {

    private var messageLog : ObservableList<ChatMessage> = FXCollections.observableList(listOf())
    private lateinit var stage : Stage

    private val lock = ReentrantLock()

    override fun onClientStartup() { }

    override fun onConnectionEstablished(
        connectionHandler : ConnectionHandler,
        incomingTrafficHandler : IncomingTrafficHandler,
    ) {
        synchronized(lock) {
            val root = VBox()
            val infoLabel = Label()
            val messageBox = ListView(messageLog)
            val feedbackLabel = Label()
            val sendMessageBox = TextEntryBox(true, "Send", "Send a message...")

            Platform.runLater {
                root.children += infoLabel
                root.children += messageBox
                root.children += feedbackLabel
                root.children += sendMessageBox
                messageBox.cellFactory = Callback { ChatMessageListCell() }
                sendMessageBox.submitButton.onAction = EventHandler {
                    connectionHandler.sendTraffic(SendMessageRequest(sendMessageBox.textField.text))
                    sendMessageBox.textField.clear()
                }

                stage = Stage()
                stage.scene = Scene(root)
                stage.show()
                //TODO("Implement actual application logic")
            }
            incomingTrafficHandler.addOnTrafficReceivedListener {
                when (it) {
                    is OkResponse -> {
                        Platform.runLater { feedbackLabel.text = "Ok" }
                    }
                    is ErrorNotification -> {
                        if (it.fatal) {
                            Platform.runLater { feedbackLabel.text = "fatal error:\n$it" }
                        } else {
                            Platform.runLater { feedbackLabel.text = "error:\n$it" }
                        }
                    }
                    is FetchMessageLogResponse -> {
                        messageLog.addAll(it.messageLog)
                    }
                    is MessageBroadcast -> {
                        messageLog += it.msg
                    }
                }
            }
            connectionHandler.sendTraffic(LoginRequest("deez"))
            connectionHandler.sendTraffic(FetchMessageLogRequest(-1))
        }
    }

    override fun onConnectionClosing() { }
}