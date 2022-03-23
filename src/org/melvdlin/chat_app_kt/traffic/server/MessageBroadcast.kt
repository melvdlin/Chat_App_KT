package org.melvdlin.chat_app_kt.traffic.server

import org.melvdlin.chat_app_kt.plugins.server.chat.ChatMessage

data class MessageBroadcast(val msg : ChatMessage) : ServerTraffic
