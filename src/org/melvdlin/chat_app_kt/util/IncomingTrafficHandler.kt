package org.melvdlin.chat_app_kt.util

import org.melvdlin.chat_app_kt.traffic.Traffic
import org.melvdlin.chat_app_kt.traffic.client.ClientTraffic
import java.io.InputStream
import java.io.ObjectInputStream
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
                } catch (e : InterruptedException) {
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