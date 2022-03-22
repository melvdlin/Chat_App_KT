package org.melvdlin.chat_app_kt.server

import org.melvdlin.chat_app_kt.plugin.server.ServerPlugin
import java.net.Socket
import java.util.Collections

class ConnectionManager(private val messageLog : MessageLog, private val plugins : Collection<ServerPlugin>) {

    private val handlers : MutableList<ConnectionHandler> = Collections.synchronizedList(mutableListOf())

    fun dispatch(client : Socket) {
        val handler = ConnectionHandler(client, messageLog, plugins) { handlers.remove(Thread.currentThread() as ConnectionHandler) }
        handlers.add(handler)
        handler.start()
    }

    fun broadcastServerInterrupted() {
        handlers.forEach {
            it.interrupt()
        }
    }
}