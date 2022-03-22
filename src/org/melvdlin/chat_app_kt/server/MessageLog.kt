package org.melvdlin.chat_app_kt.server

import java.util.concurrent.locks.ReentrantLock

class MessageLog {

    private val log = mutableListOf<ChatMessage>()
    private val onMessageAddedListeners = mutableListOf<(ChatMessage) -> Unit>()
    private val lock = ReentrantLock()

    fun addMessage(msg : ChatMessage) {
        synchronized(lock) {
            log += msg
            onMessageAddedListeners.forEach { it(msg) }
        }
    }

    fun toList() = synchronized(lock) { log.toList() }
    fun addOnMessageAddedListener(listener : (ChatMessage) -> Unit) = synchronized(lock) { onMessageAddedListeners += listener }
    fun removeOnMessageAddedListener(listener : (ChatMessage) -> Unit) = synchronized(lock) { onMessageAddedListeners -= listener }
}