package org.melvdlin.chat_app_kt.plugins.server.chat

class SystemMessage(sender : String, body : String, timestamp : Long, val error : Boolean) : ChatMessage(sender, body, timestamp) {
    companion object {
        const val defaultErrorColor = "#FF0000"
    }
}