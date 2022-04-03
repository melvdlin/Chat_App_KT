package org.melvdlin.chat_app_kt.chatplugin.traffic.server.responses

import org.melvdlin.chat_app_kt.chatplugin.shared.ChatMessage
import org.melvdlin.chat_app_kt.core.traffic.Request
import org.melvdlin.chat_app_kt.core.traffic.server.responses.ServerResponse

data class FetchMessageLogResponse(override val to : Request, val messageLog : List<ChatMessage>) : ServerResponse