package org.melvdlin.chat_app_kt.util

import org.melvdlin.chat_app_kt.plugins.Plugin
import org.melvdlin.chat_app_kt.traffic.Traffic
import org.melvdlin.chat_app_kt.traffic.server.MessageBroadcast
import org.melvdlin.chat_app_kt.traffic.server.ServerTraffic
import java.io.ObjectOutputStream
import java.net.Socket
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class ConnectionHandler(private val socket : Socket, private val plugins : Collection<Plugin>, private val onClosing : () -> Unit) : Thread(), AutoCloseable {

    private val trafficQueue : BlockingQueue<Traffic> = LinkedBlockingQueue()
    private val incomingTrafficHandler = IncomingTrafficHandler(socket.getInputStream())

    fun sendTraffic(traffic : Traffic) {
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
                val traffic = trafficQueue.take()
                it.writeObject(traffic)
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