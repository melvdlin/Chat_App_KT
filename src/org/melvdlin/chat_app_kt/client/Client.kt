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
import java.net.InetAddress
import java.net.Socket
import java.util.concurrent.locks.ReentrantLock

object Client {

    private lateinit var plugins : Collection<ClientPlugin>
    private var started = false

    private lateinit var server : Socket
    private val serverLock = ReentrantLock()

    fun start(plugins : Collection<ClientPlugin>) {
        if (started) {
            throw IllegalStateException()
        }
        started = true
        this.plugins = plugins
        Application.launch(ClientFXApp::class.java)
    }

    fun onConnected(server : Socket, primaryScene : Scene) {
        synchronized(serverLock) {
            this.server = server
        }
        plugins.forEach {
            it.onConnected(this.server, primaryScene)
        }
    }
}

object ClientFXApp : Application() {

    object Constants {
        const val maxPortNumber = 0xFFFF
        const val digits = "0123456789"
    }

    private lateinit var primaryStage : Stage
    private val primaryScene = Scene(VBox())

    override fun start(primaryStage : Stage) {
        this.primaryStage = primaryStage
        primaryStage.scene = primaryScene
    }

    private fun buildLoginScreen() {
        val root = VBox()
        primaryScene.root = root

        val infoLabel = Label("Not connected.")
        root.children += infoLabel

        val feedbackLabel = Label()
        root.children += feedbackLabel

        val portEntryBox = HBox()
        root.children += portEntryBox

        val portEntryField = TextField("Enter a port number...")
        root.children += portEntryField

        val portEntrySubmitButton = Button("Connect")
        portEntryBox.children += portEntrySubmitButton

        // intercept non-digit characters and enforce max port number
        portEntryField.addEventFilter(KeyEvent.KEY_TYPED) {
            if (!Constants.digits.contains(it.character)) {
                it.consume()
            }
            if (Constants.maxPortNumber < (portEntryField.text + it.character).toInt()) {
                portEntryField.text = Constants.maxPortNumber.toString()
                portEntryField.positionCaret(portEntryField.text.length)
                it.consume()
            }
        }

        // disable submission if port entry field is blank and reenable if it changes to not blank
        portEntryField.textProperty().addListener { _, _, newValue ->
            if (portEntrySubmitButton.isDisabled && newValue.isNotBlank()) {
                portEntrySubmitButton.isDisable = false
            }
            if (!portEntrySubmitButton.isDisabled && newValue.isBlank()) {
                portEntrySubmitButton.isDisable = true
            }
        }

        // enable submission via pressing enter in the text field
        portEntryField.onKeyPressed = EventHandler { event ->
            if (event.code == KeyCode.ENTER) {
                portEntrySubmitButton.fire()
            }
        }

        // enable submission via button and disable it (due to text field being initially empty)
        portEntrySubmitButton.isDisable = true
        val submitPort = {
            val host = InetAddress.getLocalHost()
            val port = portEntryField.text.toInt()

            portEntryField.isDisable = true
            portEntrySubmitButton.isDisable = true

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
                    }
                },
                onFailed = {
                    Platform.runLater {
                        feedbackLabel.text = "Failed to connect to port $port at host ${host.hostName}:\n\t${it}"
                        portEntryField.isDisable = false
                        portEntrySubmitButton.isDisable = false
                    }
                }
            )
        }
        portEntrySubmitButton.onAction = EventHandler {
            submitPort
        }
    }

    private fun connectAsync(
        host : InetAddress,
        port : Int,
        onStart : () -> Unit,
        onSuccess : () -> Unit,
        onFailed : (e : Throwable) -> Unit) {

        Thread {
            onStart()
            try {
                Client.onConnected(Socket(host, port), primaryScene)
            } catch (e : Throwable) {
                onFailed(e)
            } finally {
                onSuccess()
            }
        }.start()
    }
}