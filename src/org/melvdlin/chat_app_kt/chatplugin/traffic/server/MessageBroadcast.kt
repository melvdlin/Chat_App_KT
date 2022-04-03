package org.melvdlin.chat_app_kt.chatplugin.traffic.server

import org.melvdlin.chat_app_kt.chatplugin.shared.ChatMessage
import org.melvdlin.chat_app_kt.core.traffic.server.ServerTraffic

data class MessageBroadcast(val msg : ChatMessage) : ServerTraffic
