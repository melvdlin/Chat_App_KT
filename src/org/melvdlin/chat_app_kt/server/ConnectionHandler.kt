package org.melvdlin.chat_app_kt.server

import org.melvdlin.chat_app_kt.plugins.server.ServerPlugin
import org.melvdlin.chat_app_kt.traffic.server.ServerTraffic
import java.io.ObjectOutputStream
import java.net.Socket
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class ConnectionHandler(private val client : Socket, private val plugins : Collection<ServerPlugin>, private val onClosing : () -> Unit) : Thread(), AutoCloseable {

    private val trafficQueue : BlockingQueue<ServerTraffic> = LinkedBlockingQueue()
    private val incomingTrafficHandler = IncomingTrafficHandler(client.getInputStream())

    fun sendTraffic(traffic : ServerTraffic) {
        if (isInterrupted) {
            throw IllegalStateException()
        }
        trafficQueue.put(traffic)
    }

    override fun run() {



        plugins.forEach { it.onConnectionAccepted(this, incomingTrafficHandler) }

        incomingTrafficHandler.start()
        ObjectOutputStream(client.getOutputStream()).use {
            while (!isInterrupted || !trafficQueue.isEmpty()) {
                it.writeObject(trafficQueue.take())
            }
        }

        onClosing()
        client.close()
    }

    override fun close() {
        incomingTrafficHandler.close()
        interrupt()
    }
}