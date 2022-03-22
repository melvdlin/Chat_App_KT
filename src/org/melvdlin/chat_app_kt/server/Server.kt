package org.melvdlin.chat_app_kt.server

import org.melvdlin.chat_app_kt.plugin.server.ServerPlugin
import java.net.ServerSocket
import java.util.PriorityQueue

class Server(private val port : Int, private val plugins : Collection<ServerPlugin>) : Thread() {

    private val messageLog = MessageLog()
    private val manager = ConnectionManager(messageLog, plugins)

    override fun run() {

        ServerSocket(port).use {
            while (!isInterrupted) {
                manager.dispatch(it.accept())
            }
            manager.broadcastServerInterrupted()
        }
    }
}