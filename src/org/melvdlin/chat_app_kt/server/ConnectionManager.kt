package org.melvdlin.chat_app_kt.server

import org.melvdlin.chat_app_kt.plugins.server.ServerPlugin
import java.net.Socket
import java.util.concurrent.locks.ReentrantLock

class ConnectionManager(private val plugins : Collection<ServerPlugin>) {

    private val handlers : MutableList<ConnectionHandler> = mutableListOf()
    private val lock = ReentrantLock()

    fun dispatch(client : Socket) {
        synchronized(lock) {
            val handler = ConnectionHandler(client, plugins) {
                synchronized(lock) {
                    handlers.remove(Thread.currentThread() as ConnectionHandler)
                }
            }
            handlers.add(handler)
            handler.start()
        }
    }

    fun broadcastServerInterrupted() {
        synchronized(lock) {
            handlers.forEach {
                it.interrupt()
            }
        }
    }
}