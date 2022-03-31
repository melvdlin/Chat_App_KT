package org.melvdlin.chat_app_kt.plugins.server.chat

import org.melvdlin.chat_app_kt.plugins.server.ServerPlugin
import org.melvdlin.chat_app_kt.util.ConnectionHandler
import org.melvdlin.chat_app_kt.util.IncomingTrafficHandler
import java.util.concurrent.locks.ReentrantLock

class ServerChatPlugin : ServerPlugin {

    private val messageLog = MessageLog()
    private val lock = ReentrantLock()

    override fun onServerStartup() {
    }

    override fun onConnectionEstablished(
        connectionHandler : ConnectionHandler,
        incomingTrafficHandler : IncomingTrafficHandler,
    ) {
        synchronized(lock) {
            val chatterConnection = ChatterConnection(messageLog, connectionHandler)
            messageLog.addOnMessageAddedListener { chatterConnection.sendMessage(it) }
            incomingTrafficHandler.addOnTrafficReceivedListener { chatterConnection.onTrafficReceived(it) }
        }
    }

    override fun onConnectionClosing() { }
}