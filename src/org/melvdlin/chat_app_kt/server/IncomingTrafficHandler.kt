package org.melvdlin.chat_app_kt.server

import org.melvdlin.chat_app_kt.traffic.client.ClientTraffic
import java.io.InputStream
import java.io.ObjectInputStream
import java.util.concurrent.locks.ReentrantLock

class IncomingTrafficHandler(private val inputStream : InputStream) : Thread() {

    private val onTrafficReceivedListeners = mutableListOf<(ClientTraffic) -> Unit>()
    private val lock = ReentrantLock()

    override fun run() {
        ObjectInputStream(inputStream).use {
            var traffic : ClientTraffic
            while (!isInterrupted) {
                traffic = it.readObject() as ClientTraffic
                synchronized(lock) {
                    onTrafficReceivedListeners.forEach {
                        it(traffic)
                    }
                }
            }
        }
    }

    fun addOnTrafficReceivedListener(listener : (ClientTraffic) -> Unit) = synchronized(lock) { onTrafficReceivedListeners += listener }
    fun removeOnTrafficReceivedListener(listener : (ClientTraffic) -> Unit) = synchronized(lock) { onTrafficReceivedListeners -= listener }
}