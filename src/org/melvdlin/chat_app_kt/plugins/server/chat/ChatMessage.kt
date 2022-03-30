package org.melvdlin.chat_app_kt.plugins.server.chat

class ChatMessage(val sender : String, val body : String, val timestamp : Long) {
    override fun equals(other : Any?) : Boolean {
        return other is ChatMessage && sender == other.sender && body == other.body && timestamp == other.timestamp
    }
}