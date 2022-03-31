package org.melvdlin.chat_app_kt.plugins.client.chat

import javafx.application.Application
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
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

    private var messageLog : ObservableList<ChatMessage> = FXCollections.observableList(mutableListOf())
    private lateinit var stage : Stage

    private val lock = ReentrantLock()

    override fun onClientStartup() { }

    override fun onConnectionEstablished(
        connectionHandler : ConnectionHandler,
        incomingTrafficHandler : IncomingTrafficHandler,
    ) {
        synchronized(lock) {
            val viewMessageLog : ObservableList<ChatMessage> = FXCollections.observableList(mutableListOf())

            val root = VBox()
            val infoLabel = Label()
            val topBarSpacer = Region()
            val disconnectButton = Button("Disconnect")
            val topBar = HBox()
            val messageBox = ListView(viewMessageLog)
            val feedbackLabel = Label()
            val sendMessageBox = TextEntryBox(true, "Send", "Send a message...")

            messageLog.addListener( ListChangeListener {
                while (it.next()) {
                    if (it.wasAdded()) {
                        Platform.runLater {
                            viewMessageLog.addAll(it.from, it.addedSubList)
                        }
                    }
                    if (it.wasRemoved()) {
                        Platform.runLater {
                            viewMessageLog.removeAll(it.removed)
                        }
                    }
                    messageBox.scrollTo(messageBox.items.lastIndex)
                }
            } )

            Platform.runLater {
                root.children += topBar
                root.children += messageBox
                root.children += feedbackLabel
                root.children += sendMessageBox

                topBar.children += infoLabel
                topBar.children += topBarSpacer
                topBar.children += disconnectButton

                HBox.setHgrow(topBarSpacer, Priority.ALWAYS)
                disconnectButton.onAction = EventHandler{
                    connectionHandler.close()
                    Platform.exit()
                }

                messageBox.cellFactory = Callback { ChatMessageListCell() }
                sendMessageBox.submitButton.onAction = EventHandler {
                    connectionHandler.sendTraffic(SendMessageRequest(sendMessageBox.textField.text))
                    sendMessageBox.textField.clear()
                }

                stage = Stage()
                stage.scene = Scene(root)
                stage.show()
                sendMessageBox.textField.requestFocus()
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
                        println(it.msg.body)
                        Platform.runLater {
                            messageLog += it.msg
                        }
                    }
                }
            }
            connectionHandler.sendTraffic(LoginRequest("deez"))
            connectionHandler.sendTraffic(FetchMessageLogRequest(-1))
        }
    }

    override fun onConnectionClosing() { }
}