package org.melvdlin.chat_app_kt.client

import javafx.application.Application
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.melvdlin.chat_app_kt.plugins.client.ClientPlugin
import org.melvdlin.chat_app_kt.plugins.client.chat.fx.TextEntryBox
import org.melvdlin.chat_app_kt.testbed.plugins
import org.melvdlin.chat_app_kt.util.ConnectionHandler
import java.net.InetAddress
import java.net.Socket
import java.util.concurrent.locks.ReentrantLock

object Client {

    object Constants {
        const val timeoutMillis = 15000L
        const val backlog = -1
    }

    private var plugins : Collection<ClientPlugin> = mutableListOf()

    private var started = false

    private lateinit var connection : ConnectionHandler
    private val connectionLock = ReentrantLock()

    fun start() = start(listOf())

    fun start(plugins : Collection<ClientPlugin>) {
        if (started) {
            throw IllegalStateException()
        }
        started = true
        this.plugins = plugins
        Platform.setImplicitExit(false)
        Application.launch(ClientFXApp::class.java)
    }

    fun forEachPlugin(action : (ClientPlugin) -> Unit) {
        plugins.forEach(action)
    }

    fun onConnected(server : Socket, onConnectionClosing : () -> Unit) {
        synchronized(connectionLock) {
            connection = ConnectionHandler(server, plugins)
            connection.addOnClosingListener(onConnectionClosing)
            connection.start()
        }
    }
}

class ClientFXApp : Application() {


    object Constants {
        const val maxPortNumber = 0xFFFF
        const val digits = "0123456789"
    }

    private lateinit var primaryStage : Stage
    private val primaryScene = Scene(VBox())


    override fun start(primaryStage : Stage) {
        this.primaryStage = primaryStage
        primaryStage.scene = primaryScene

        Client.forEachPlugin { it.onClientStartup() }

        buildConnectScreen()
        primaryStage.show()
    }

    private fun buildConnectScreen() {
        val root = VBox()
        primaryScene.root = root

        val infoLabel = Label("Not connected.")
        root.children += infoLabel

        val feedbackLabel = Label()
        root.children += feedbackLabel

        val portEntryBox = TextEntryBox(
            disableWhenBlank = true,
            buttonText = "Connect",
            promptText = "Enter a port number...")
        root.children += portEntryBox

        // intercept non-digit characters and enforce max port number
        portEntryBox.textField.addEventFilter(KeyEvent.KEY_TYPED) {
            it?.let {
                if (!Constants.digits.contains(it.character)) {
                    it.consume()
                }
                else if (Constants.maxPortNumber < (portEntryBox.textField.text + it.character).toInt()) {
                    portEntryBox.textField.text = Constants.maxPortNumber.toString()
                    portEntryBox.textField.positionCaret(portEntryBox.textField.text.length)
                    it.consume()
                }
            }
        }

        val submitPort = {
            val host = InetAddress.getLocalHost()
            val port = portEntryBox.textField.text.toInt()

            portEntryBox.textField.isDisable = true
            portEntryBox.submitButton.isDisable = true

            connectAsync(
                host = host,
                port = port,
                onStart = {
                    Platform.runLater {
                        feedbackLabel.text = "Connecting to port $port at host ${host.hostName}..."
                    }
                },
                onSuccess = {
                    Platform.runLater {
                        feedbackLabel.text = "Successfully connected to port $port at host ${host.hostName}."
                        primaryStage.hide()
                    }
                },
                onFailed = {
                    Platform.runLater {
                        feedbackLabel.text = "Failed to connect to port $port at host ${host.hostName}:\n${it}"
                        portEntryBox.textField.isDisable = false
                        portEntryBox.submitButton.isDisable = false
                    }
                },
                onConnectionClosing = {
                    Client.forEachPlugin { it.onConnectionClosing() }
                    Platform.runLater {
                        portEntryBox.textField.isDisable = false
                        portEntryBox.submitButton.isDisable = false
                        primaryStage.show()
                    }
                }
            )
        }
        portEntryBox.submitButton.onAction = EventHandler {
            submitPort()
        }
    }

    private fun connectAsync(
        host : InetAddress,
        port : Int,
        onConnectionClosing : () -> Unit,
        onStart : () -> Unit,
        onSuccess : () -> Unit,
        onFailed : (e : Throwable) -> Unit) {

        Thread {
            onStart()
            try {
                Client.onConnected(Socket(host, port), onConnectionClosing)
                onSuccess()
            } catch (e : Throwable) {
                onFailed(e)
            }
        }.start()
    }
}

























