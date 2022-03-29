package org.melvdlin.chat_app_kt.plugins.client.chat

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
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
import org.melvdlin.chat_app_kt.traffic.client.requests.FetchMessageLogRequest
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
            connectionHandler.sendTraffic(FetchMessageLogRequest(-1))
            Platform.runLater {
                stage = Stage()
                val root = VBox()
                stage.scene = Scene(root)

                val infoLabel = Label()
                root.children += infoLabel

                val messageBox = ListView(messageLog)
                root.children += messageBox
                messageBox.cellFactory = Callback { ChatMessageListCell() }

                val feedbackLabel = Label()
                root.children += feedbackLabel

                val sendMessageBox = TextEntryBox(true, "Send", "Send a message...")
                root.children += sendMessageBox

                TODO("Implement actual application logic")
            }
        }
    }

    override fun onConnectionClosing() { }
}