package org.melvdlin.chat_app_kt.core

import org.melvdlin.chat_app_kt.core.traffic.Traffic
import java.io.EOFException
import java.io.InputStream
import java.io.ObjectInputStream
import java.net.SocketException
import java.util.concurrent.locks.ReentrantLock

class IncomingTrafficHandler(private val inputStream : InputStream) : Thread(), AutoCloseable {

    private val onTrafficReceivedListeners = mutableListOf<(Traffic) -> Unit>()
    private val lock = ReentrantLock()

    override fun run() {
        ObjectInputStream(inputStream).use {
            var traffic : Traffic
            while (!isInterrupted) {
                try {
                    traffic = it.readObject() as Traffic
                    synchronized(lock) {
                        onTrafficReceivedListeners.forEach { listener ->
                            listener(traffic)
                        }
                    }
                } catch (_ : SocketException) {
                    interrupt()
                } catch (_ : EOFException) {
                    interrupt()
                }
            }
        }
    }

    fun addOnTrafficReceivedListener(listener : (Traffic) -> Unit) {
        synchronized(lock) {
            onTrafficReceivedListeners += listener
        }
    }

    fun removeOnTrafficReceivedListener(listener : (Traffic) -> Unit) {
        synchronized(lock) {
            onTrafficReceivedListeners -= listener
        }
    }
    override fun close() {
        interrupt()
    }
}