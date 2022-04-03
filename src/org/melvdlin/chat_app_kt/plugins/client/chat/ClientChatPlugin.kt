package org.melvdlin.chat_app_kt.plugins.client.chat

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.event.EventHandler
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
import org.melvdlin.chat_app_kt.client.Client
import org.melvdlin.chat_app_kt.plugins.client.ClientPlugin
import org.melvdlin.chat_app_kt.plugins.client.chat.fx.ChatMessageListCell
import org.melvdlin.chat_app_kt.plugins.client.chat.fx.TextEntryBox
import org.melvdlin.chat_app_kt.plugins.server.chat.ChatMessage
import org.melvdlin.chat_app_kt.plugins.server.chat.SystemMessage
import org.melvdlin.chat_app_kt.testbed.someString
import org.melvdlin.chat_app_kt.traffic.ErrorNotification
import org.melvdlin.chat_app_kt.traffic.Response
import org.melvdlin.chat_app_kt.traffic.client.requests.DisconnectRequest
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

    private val lock = ReentrantLock()

    private class LoginUI(
        connectionHandler : ConnectionHandler,
        incomingTrafficHandler : IncomingTrafficHandler,
        messageLog : ObservableList<ChatMessage>,
        chatUI : ChatUI
    ) : Stage()
    {
        val root = HBox()
        val nameEntryBox = TextEntryBox(true, "Login", "Enter a username...")
        val exitButton = Button("Exit")

        init {
            connectionHandler.addOnClosingListener { hide() }
            scene = Scene(root)
            root.children += nameEntryBox
            root.children += exitButton

            val fetchResponseHandler : (Response) -> Unit = {
                when (it) {
                    is FetchMessageLogResponse -> {
                        messageLog.addAll(it.messageLog)
                    }
                    else -> {
                        if (it !is ErrorNotification) {
                            ErrorPopup(false, "Could not fetch message log.")
                        }
                    }
                }
                if (it !is ErrorNotification || !it.fatal) {
                    incomingTrafficHandler.addOnTrafficReceivedListener {
                        when (it) {
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
            }

            val fetchTimeoutHandler = {
                ErrorPopup(false, "Could not fetch message log: Server timed out").show()
                chatUI.show()
            }

            val loginResponseHandler = { response : Response ->
                when (response) {
                    is OkResponse -> {
                        connectionHandler.sendTimeoutRequest(
                            FetchMessageLogRequest(Client.Constants.backlog),
                            Client.Constants.timeoutMillis,
                            fetchResponseHandler,
                            fetchTimeoutHandler
                        )
                    }
                    else -> {
                        if (response !is ErrorNotification) {
                            Platform.runLater {
                                ErrorPopup(false, "Login Request failed.").show()
                            }
                        }
                        if (response !is ErrorNotification || !response.fatal) {
                            nameEntryBox.submitButton.isDisable = false
                        }
                    }
                }
            }

            val loginTimeoutHandler = {
                ErrorPopup(false, "Login Request failed: Server timed out") {
                    nameEntryBox.submitButton.isDisable = false
                }.show()
            }

            nameEntryBox.submitButton.onAction = EventHandler {
                nameEntryBox.submitButton.isDisable = true
                connectionHandler.sendTimeoutRequest(LoginRequest(nameEntryBox.textField.text),
                    Client.Constants.timeoutMillis,
                    loginResponseHandler,
                    loginTimeoutHandler
                )
            }

            exitButton.onAction = EventHandler {
                connectionHandler.close()
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

        var isDisabled = false
            set(value) {
                field = value
                disconnectButton.isDisable = value
                sendMessageBox.textField.isDisable = value
                sendMessageBox.submitButton.isDisable = value
            }

        init {
            connectionHandler.addOnClosingListener {
                hide()
            }
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
                connectionHandler.sendTraffic(DisconnectRequest())
                connectionHandler.close()
            }

            messageBox.cellFactory = Callback { ChatMessageListCell() }

            val sendMessageTimeoutHandler = { msg : String ->
                Platform.runLater {
                    val errorMsg = SystemMessage("SYSTEM", "Unable to send message:\n$msg", System.currentTimeMillis(), true)
                    viewMessageLog += errorMsg
                }
            }

            val sendMessageResponseHandler = { msg : String, response : Response ->
                if (response !is OkResponse) {
                    sendMessageTimeoutHandler(msg)
                }
            }

            sendMessageBox.submitButton.onAction = EventHandler {
                connectionHandler.sendTimeoutRequest(
                    request = SendMessageRequest(sendMessageBox.textField.text),
                    timeoutMillis = Client.Constants.timeoutMillis,
                    responseHandler = { sendMessageResponseHandler(sendMessageBox.textField.text, it) },
                    timeoutHandler = { sendMessageTimeoutHandler(sendMessageBox.textField.text) }
                )
                sendMessageBox.textField.clear()
            }
        }
    }

    private class ErrorPopup(val fatal : Boolean, val info : String, val onConfirmed : () -> Unit = {}) : Stage() {

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
                onConfirmed()
            }
        }
    }

    override fun onClientStartup() { }

    override fun onConnectionEstablished(
        connectionHandler : ConnectionHandler,
        incomingTrafficHandler : IncomingTrafficHandler,
    ) {
        synchronized(lock) {
            incomingTrafficHandler.addOnTrafficReceivedListener {
                when (it) {
                    is ErrorNotification -> {
                        if (it.fatal) { connectionHandler.close() }
                        Platform.runLater { ErrorPopup(it.fatal, it.info) }
                    }
                }
            }
            Platform.runLater {
                val chatUI = ChatUI(connectionHandler, messageLog)
                LoginUI(connectionHandler, incomingTrafficHandler, messageLog, chatUI).show()
            }
            connectionHandler.sendTraffic(LoginRequest("deez"))
            connectionHandler.sendTraffic(FetchMessageLogRequest(-1))
        }
    }

    override fun onConnectionClosing() { }
}