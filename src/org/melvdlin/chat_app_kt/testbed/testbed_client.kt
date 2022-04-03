package org.melvdlin.chat_app_kt.testbed

import org.melvdlin.chat_app_kt.core.client.Client
import org.melvdlin.chat_app_kt.chatplugin.client.ClientChatPlugin

fun main() {
    Client.start(listOf(ClientChatPlugin()))
}