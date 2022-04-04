package org.melvdlin.chat_app_kt.chatplugin.client.view.fx

import org.melvdlin.chat_app_kt.chatplugin.shared.ChatMessage

class DisplayableChatMessage(val msg : ChatMessage) : DisplayableMessage {
    override val body = msg.body
    override val sender = msg.sender
    override val timestamp = msg.timestamp
}