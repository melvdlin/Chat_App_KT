package org.melvdlin.chat_app_kt.plugins.server.chat

import java.util.concurrent.locks.ReentrantLock
import kotlin.math.min

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

    fun toList() : List<ChatMessage> {
        synchronized(lock) {
            return log.toList()
        }
    }

    fun toList(backlog : Int) : List<ChatMessage> {
        synchronized(lock) {
            return if (backlog < 0) {
                log.toList()
            } else {
                log.subList(min(0, 1 + log.lastIndex - backlog), 1 + log.lastIndex).toList()
            }
        }
    }

    fun addOnMessageAddedListener(listener : (ChatMessage) -> Unit) {
        synchronized(lock) {
            onMessageAddedListeners += listener
        }
    }

    fun removeOnMessageAddedListener(listener : (ChatMessage) -> Unit) {
        synchronized(lock) {
            onMessageAddedListeners -= listener
        }
    }
}