package org.melvdlin.chat_app_kt.core.server

import org.melvdlin.chat_app_kt.core.plugin.ServerPlugin
import org.melvdlin.chat_app_kt.core.deprecated.ConnectionHandler
import java.net.Socket
import java.util.concurrent.locks.ReentrantLock

class ConnectionManager(private val plugins : Collection<ServerPlugin>) {

    private val handlers : MutableList<ConnectionHandler> = mutableListOf()
    private val lock = ReentrantLock()

    fun dispatch(client : Socket) {
        synchronized(handlers) {
            val handler = ConnectionHandler(client, plugins)
            handler.addOnClosingListener {
                synchronized(handlers) {
                    handlers -= handler
                }
            }
            handlers += handler
            handler.start()
        }
    }

    fun broadcastServerInterrupted() {
        synchronized(handlers) {
            handlers.forEach {
                it.interrupt()
            }
        }
    }
}
