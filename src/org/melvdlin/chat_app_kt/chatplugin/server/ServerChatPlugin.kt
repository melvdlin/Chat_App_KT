package org.melvdlin.chat_app_kt.chatplugin.server

import org.melvdlin.chat_app_kt.core.plugin.ServerPlugin
import org.melvdlin.chat_app_kt.core.netcode.ConnectionHandler

class ServerChatPlugin : ServerPlugin {

    private val messageLog = MessageLog()
    private val lock = Any()

    override fun onServerStartup() {
    }

    override fun onConnectionEstablished(
        connectionHandler : ConnectionHandler,
    ) {
        synchronized(lock) {
            val chatterConnection = ChatterConnection(messageLog, connectionHandler)
            messageLog.addOnMessageAddedListener { chatterConnection.sendMessage(it) }
            connectionHandler.addOnTrafficReceivedListener{chatterConnection.onTrafficReceived(it)}
        }
    }

    override fun onConnectionClosed() = Unit

    override fun onConnectionError() = Unit
}