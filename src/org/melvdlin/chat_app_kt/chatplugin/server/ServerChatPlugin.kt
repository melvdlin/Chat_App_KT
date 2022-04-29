package org.melvdlin.chat_app_kt.chatplugin.server

import org.melvdlin.chat_app_kt.chatplugin.shared.ChatMessage
import org.melvdlin.chat_app_kt.core.plugin.ServerPlugin
import org.melvdlin.chat_app_kt.core.netcode.ConnectionHandler
import java.util.LinkedList

class ServerChatPlugin : ServerPlugin {

    private val messageLog = MessageLog()
    private val cleanupActions : MutableList<() -> Boolean> = mutableListOf()
    private val lock = Any()


    override fun onServerStartup() {
    }

    override fun onConnectionEstablished(
        connectionHandler : ConnectionHandler,
    ) {
        synchronized(lock) {
            val chatterConnection = ChatterConnection(messageLog, connectionHandler)

            val sendMessage : (ChatMessage) -> Unit = {
                chatterConnection.sendMessage(it)
            }
            val cleanupAction : () -> Boolean = {
                messageLog.removeOnMessageAddedListener(sendMessage)
                true
            }
            messageLog.addOnMessageAddedListener(sendMessage)
            cleanupActions += cleanupAction
            connectionHandler.addOnTrafficReceivedListener{ chatterConnection.onTrafficReceived(it) }
        }
    }

    override fun onConnectionClosed() {
        cleanup()
    }

    override fun onConnectionError() {
        cleanup()
    }

    private fun cleanup() {
        synchronized(lock) {
            val toRemove : MutableList<() -> Boolean> = LinkedList()
            cleanupActions.forEach { if(it()) toRemove += it }
            toRemove.forEach { cleanupActions -= it }
        }
    }
}