package org.melvdlin.chat_app_kt.util

import org.melvdlin.chat_app_kt.plugins.Plugin
import org.melvdlin.chat_app_kt.traffic.Request
import org.melvdlin.chat_app_kt.traffic.Response
import org.melvdlin.chat_app_kt.traffic.Traffic
import java.io.ObjectOutputStream
import java.net.Socket
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.schedule


class ConnectionHandler(private val socket : Socket, private val plugins : Collection<Plugin>, private val onClosing : () -> Unit) : Thread(), AutoCloseable {

    private var closing = false

    private val trafficQueue : BlockingQueue<Traffic> = LinkedBlockingQueue()
    private val incomingTrafficHandler = IncomingTrafficHandler(socket.getInputStream())

    private val requestTimer : Timer by lazy {
        synchronized(requestTimer) {
            requestTimerIsInitialized = true
            Timer(true)
        }
    }
    private var requestTimerIsInitialized = false
    private val openRequests : MutableMap<Request, (Response) -> Unit> = mutableMapOf()

    init {
        incomingTrafficHandler.addOnTrafficReceivedListener {
            synchronized(openRequests) {
                if (it is Response) {
                    openRequests.remove(it.to)?.invoke(it)
                }
            }
        }
    }


    fun sendTimeoutRequest(request : Request, timeoutMillis : Long, responseHandler : (Response) -> Unit, timeoutHandler : () -> Unit) {
        synchronized(closing) {
            if (closing) {
                throw IllegalStateException()
            }
        }
        synchronized(openRequests) {
            openRequests += request to responseHandler
            requestTimer.schedule(timeoutMillis) {
                synchronized(openRequests) {
                    openRequests.remove(request)?.let {
                        timeoutHandler()
                    }
                }
            }
        }

    }

    fun sendTraffic(traffic : Traffic) {
        if (closing) {
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
        synchronized(closing) {
            closing = true
            incomingTrafficHandler.close()
            synchronized(requestTimer) {
                if (requestTimerIsInitialized) {
                    requestTimer.cancel()
                }
            }
            interrupt()
        }
    }
}