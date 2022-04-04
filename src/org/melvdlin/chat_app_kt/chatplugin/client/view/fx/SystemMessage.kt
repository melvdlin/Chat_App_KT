package org.melvdlin.chat_app_kt.chatplugin.client.view.fx

import org.melvdlin.chat_app_kt.chatplugin.shared.ChatMessage

class SystemMessage(
    override val sender : String = "SYSTEM",
    override val body : String,
    override val timestamp : Long = System.currentTimeMillis(),
    val error : Boolean = false
    ) : DisplayableMessage
{
    companion object {
        const val defaultErrorColor = "#FF0000"
    }
}