package org.melvdlin.chat_app_kt.server

import java.io.ObjectOutputStream
import java.net.Socket
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class ConnectionHandler(private val client : Socket, private val onFinishing : () -> Unit) : Thread() {

    override fun run() {

        val tasks : BlockingQueue<ConnectionTask> = LinkedBlockingQueue()

        val incomingTrafficHandler = IncomingTrafficHandler(client.getInputStream()) {
            tasks.put(it)
        }
        incomingTrafficHandler.start()

        ObjectOutputStream(client.getOutputStream()).use {
            while (!isInterrupted) {
                when (tasks.take()) {

                }
            }
        }

        onFinishing()
    }
}