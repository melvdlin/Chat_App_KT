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
import org.melvdlin.chat_app_kt.plugins.server.chat.ChatterConnection
import org.melvdlin.chat_app_kt.plugins.server.chat.MessageLog
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

    private class LoginUI(connectionHandler : ConnectionHandler) : Stage() {

        val root = HBox()
        val nameEntryBox = TextEntryBox(true, "Login", "Enter a username...")
        val exitButton = Button("Exit")

        var confirmationPending = false
            get() = synchronized(lock) { return field }
            private set

        val lock = Any()

        init {
            scene = Scene(root)
            root.children += nameEntryBox
            root.children += exitButton

            nameEntryBox.submitButton.onAction = EventHandler {
                synchronized(lock) {
                    nameEntryBox.submitButton.isDisable = true
                    confirmationPending = true
                    connectionHandler.sendTraffic(LoginRequest(nameEntryBox.textField.text))
                }
            }

            exitButton.onAction = EventHandler {
                TODO("Implement connection exit")
            }
        }

    }

    private class ChatUI(
        connectionHandler : ConnectionHandler,
        messageLog : ObservableList<ChatMessage>
    ) : Stage()
    {
        val viewMessageLog : ObservableList<ChatMessage> = FXCollections.observableList(mutableListOf())

        val root = VBox()
        val infoLabel = Label()
        val topBarSpacer = Region()
        val disconnectButton = Button("Disconnect")
        val topBar = HBox()
        val messageBox = ListView(viewMessageLog)
        val feedbackLabel = Label()
        val sendMessageBox = TextEntryBox(true, "Send", "Send a message...")

        init {
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

            scene = Scene(root)

            root.children += topBar
            root.children += messageBox
            root.children += feedbackLabel
            root.children += sendMessageBox

            topBar.children += infoLabel
            topBar.children += topBarSpacer
            topBar.children += disconnectButton

            HBox.setHgrow(topBarSpacer, Priority.ALWAYS)
            disconnectButton.onAction = EventHandler{
                TODO("Implement connection exit")
                connectionHandler.close()
                Platform.exit()
            }

            messageBox.cellFactory = Callback { ChatMessageListCell() }
            sendMessageBox.submitButton.onAction = EventHandler {
                connectionHandler.sendTraffic(SendMessageRequest(sendMessageBox.textField.text))
                sendMessageBox.textField.clear()
            }
        }
    }

    private class ErrorPopup(val fatal : Boolean, val info : String) : Stage() {

        private val root = VBox()
        private val topLabel = Label("A${if (fatal) " fatal" else "n"} error occured:")
        private val infoLabel = Label(info)
        private val okButtonBox = HBox()
        private val verticalSpacer = Region()
        private val okButtonSpacer1 = Region()
        private val okButtonSpacer2 = Region()
        private val okButton = Button("Ok")

        init {
            title = "${if (fatal) "Fatal " else ""}Error"

            scene = Scene(root)

            root.children.addAll(topLabel, infoLabel, verticalSpacer, okButtonBox)
            okButtonBox.children.addAll(okButtonSpacer1, okButton, okButtonSpacer2)

            VBox.setVgrow(verticalSpacer, Priority.ALWAYS)
            HBox.setHgrow(okButtonSpacer1, Priority.ALWAYS)
            HBox.setHgrow(okButtonSpacer2, Priority.ALWAYS)

            okButton.onAction = EventHandler {
                hide()
                if (fatal) {
                    TODO("Implement connection exit")
                }
            }
        }
    }

    override fun onClientStartup() { }

    override fun onConnectionEstablished(
        connectionHandler : ConnectionHandler,
        incomingTrafficHandler : IncomingTrafficHandler,
    ) {
        synchronized(lock) {
            Platform.runLater {
                ErrorPopup(false, "deez").show()
                val chatUI = ChatUI(connectionHandler, messageLog)

                incomingTrafficHandler.addOnTrafficReceivedListener {
                    when (it) {
                        is OkResponse -> {
                            Platform.runLater { chatUI.feedbackLabel.text = "Ok" }
                        }
                        is ErrorNotification -> {
                            if (it.fatal) {
                                Platform.runLater { chatUI.feedbackLabel.text = "fatal error:\n$it" }
                            } else {
                                Platform.runLater { chatUI.feedbackLabel.text = "error:\n$it" }
                            }
                        }
                        is FetchMessageLogResponse -> {
                            messageLog.addAll(it.messageLog)
                        }
                        is MessageBroadcast -> {
                            Platform.runLater {
                                messageLog += it.msg
                            }
                        }
                    }
                }
                chatUI.show()
                chatUI.sendMessageBox.textField.requestFocus()
            }
            connectionHandler.sendTraffic(LoginRequest("deez"))
            connectionHandler.sendTraffic(FetchMessageLogRequest(-1))
        }
    }

    override fun onConnectionClosing() { }
}