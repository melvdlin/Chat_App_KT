package org.melvdlin.chat_app_kt.core.client

import javafx.application.Application
import javafx.application.Platform
import org.melvdlin.chat_app_kt.core.plugin.ClientPlugin
import org.melvdlin.chat_app_kt.core.netcode.ConnectionHandler
import org.melvdlin.chat_app_kt.core.netcode.impl.DefaultConnectionHandler
import java.net.Socket
import java.util.concurrent.locks.ReentrantLock

object Client {

    object Constants {
        const val timeoutMillis = 5000L
        const val backlog = -1
    }

    private var plugins : Collection<ClientPlugin> = mutableListOf()

    private var started = false

    private lateinit var connection : ConnectionHandler
    private val connectionLock = ReentrantLock()

    fun start(plugins : Collection<ClientPlugin> = listOf()) {
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

    fun onConnected(server : Socket, onConnectionClosed : () -> Unit) {
        synchronized(connectionLock) {
            connection = DefaultConnectionHandler(server, plugins)
            connection.addOnClosedListener(onConnectionClosed)
            connection.start()
        }
    }
}

























