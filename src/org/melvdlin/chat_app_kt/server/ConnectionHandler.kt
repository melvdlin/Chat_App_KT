package org.melvdlin.chat_app_kt.server

import org.melvdlin.chat_app_kt.plugin.server.ServerPlugin
import org.melvdlin.chat_app_kt.traffic.server.ServerTraffic
import java.io.ObjectOutputStream
import java.net.Socket
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class ConnectionHandler(private val client : Socket, private val messageLog : MessageLog, private val plugins : Collection<ServerPlugin>, private val onFinishing : () -> Unit) : Thread() {

    private val trafficQueue : BlockingQueue<ServerTraffic> = LinkedBlockingQueue()

    fun sendTraffic(traffic : ServerTraffic) {
        trafficQueue.put(traffic)
    }

    override fun run() {

        val incomingTrafficHandler = IncomingTrafficHandler(client.getInputStream())

        plugins.forEach { it.onConnectionAccepted(messageLog, this, incomingTrafficHandler) }

        incomingTrafficHandler.start()
        ObjectOutputStream(client.getOutputStream()).use {
            while (!isInterrupted) {
                it.writeObject(trafficQueue.take())
            }
        }

        onFinishing()
    }
}