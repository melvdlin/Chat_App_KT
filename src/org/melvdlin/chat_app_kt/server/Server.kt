package org.melvdlin.chat_app_kt.server

import java.net.ServerSocket

class Server(private val port : Int) : Thread() {

    private val manager = ConnectionManager()

    override fun run() {

        ServerSocket(port).use {
            while (!isInterrupted) {
                manager.dispatch(it.accept())
            }
            manager.broadcastServerInterrupted()
        }
    }
}