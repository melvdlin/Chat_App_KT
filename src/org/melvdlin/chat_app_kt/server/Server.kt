package org.melvdlin.chat_app_kt.server

import org.melvdlin.chat_app_kt.plugins.server.ServerPlugin
import java.net.ServerSocket

class Server(private val port : Int, private val plugins : Collection<ServerPlugin>) : Thread() {

    private val manager = ConnectionManager(plugins)

    override fun run() {

        plugins.forEach { it.onServerStartup() }

        ServerSocket(port).use {
            while (!isInterrupted) {
                manager.dispatch(it.accept())
            }
            manager.broadcastServerInterrupted()
        }
    }
}