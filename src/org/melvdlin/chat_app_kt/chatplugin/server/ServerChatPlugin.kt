package org.melvdlin.chat_app_kt.chatplugin.server

import org.melvdlin.chat_app_kt.core.plugin.ServerPlugin
import org.melvdlin.chat_app_kt.core.ConnectionHandler
import org.melvdlin.chat_app_kt.core.IncomingTrafficHandler
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