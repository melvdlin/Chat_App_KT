package org.melvdlin.chat_app_kt.traffic.server.responses

import org.melvdlin.chat_app_kt.plugins.server.chat.ChatMessage
import org.melvdlin.chat_app_kt.traffic.Request

data class FetchMessageLogResponse(override val to : Request, val messageLog : List<ChatMessage>) : ServerResponse