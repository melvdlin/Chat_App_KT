package org.melvdlin.chat_app_kt.testbed

import org.melvdlin.chat_app_kt.client.Client
import org.melvdlin.chat_app_kt.plugins.client.chat.ClientChatPlugin

fun main() {
    Client.start(listOf(ClientChatPlugin()))
}