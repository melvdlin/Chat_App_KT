package org.melvdlin.chat_app_kt.plugins.server.chat

import org.melvdlin.chat_app_kt.plugins.server.ServerPlugin
import org.melvdlin.chat_app_kt.server.ConnectionHandler
import org.melvdlin.chat_app_kt.server.IncomingTrafficHandler
import java.util.concurrent.locks.ReentrantLock

class ChatPlugin : ServerPlugin {

    private val messageLog = MessageLog()
    private val broadcastTo = mutableListOf<ChatterConnection>()
    private val lock = ReentrantLock()

    override fun onServerStartup() {
        messageLog.addOnMessageAddedListener { msg ->
            broadcastTo.forEach { chatterConnection ->
                chatterConnection.sendMessage(msg)
            }
        }
    }

    override fun onConnectionAccepted(
        connectionHandler : ConnectionHandler,
        incomingTrafficHandler : IncomingTrafficHandler,
    ) {
        synchronized(lock) {
            val chatterConnection = ChatterConnection(messageLog, connectionHandler)
            broadcastTo += chatterConnection
            messageLog.addOnMessageAddedListener { chatterConnection.sendMessage(it) }
            incomingTrafficHandler.addOnTrafficReceivedListener { chatterConnection.onTrafficReceived(it) }
        }
    }
}