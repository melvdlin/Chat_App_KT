package org.melvdlin.chat_app_kt.chatplugin.traffic.client.requests

import org.melvdlin.chat_app_kt.core.traffic.client.requests.ClientRequest

data class SendMessageRequest(val msg : String) : ClientRequest