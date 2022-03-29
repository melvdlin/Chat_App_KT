package org.melvdlin.chat_app_kt.testbed

import org.melvdlin.chat_app_kt.plugins.server.chat.ServerChatPlugin
import org.melvdlin.chat_app_kt.server.Server

const val port = 6666
val plugins = listOf(ServerChatPlugin())

fun main() {
    val server = Server(port, plugins)
    server.start()
}