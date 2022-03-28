package org.melvdlin.chat_app_kt.util

import org.melvdlin.chat_app_kt.plugins.Plugin
import org.melvdlin.chat_app_kt.traffic.server.ServerTraffic
import java.io.ObjectOutputStream
import java.net.Socket
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class ConnectionHandler(private val socket : Socket, private val plugins : Collection<Plugin>, private val onClosing : () -> Unit) : Thread(), AutoCloseable {

    private val trafficQueue : BlockingQueue<ServerTraffic> = LinkedBlockingQueue()
    private val incomingTrafficHandler = IncomingTrafficHandler(socket.getInputStream())

    fun sendTraffic(traffic : ServerTraffic) {
        if (isInterrupted) {
            throw IllegalStateException()
        }
        trafficQueue.put(traffic)
    }

    override fun run() {

        plugins.forEach { it.onConnectionEstablished(this, incomingTrafficHandler) }

        incomingTrafficHandler.start()
        ObjectOutputStream(socket.getOutputStream()).use {
            while (!isInterrupted || !trafficQueue.isEmpty()) {
                it.writeObject(trafficQueue.take())
            }
        }

        onClosing()
        socket.close()
    }

    override fun close() {
        incomingTrafficHandler.close()
        interrupt()
    }
}