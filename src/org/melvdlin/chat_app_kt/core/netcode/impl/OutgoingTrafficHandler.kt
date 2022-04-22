package org.melvdlin.chat_app_kt.core.netcode.impl

import org.melvdlin.chat_app_kt.core.traffic.Request
import org.melvdlin.chat_app_kt.core.traffic.Response
import org.melvdlin.chat_app_kt.core.traffic.Traffic
import java.io.ObjectOutputStream
import java.io.OutputStream
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.collections.HashMap
import kotlin.concurrent.schedule
import kotlin.concurrent.scheduleAtFixedRate

internal class OutgoingTrafficHandler(
    private val stream : OutputStream,
    private val onClosed : () -> Unit,
    private val onError : () -> Unit
) : AutoCloseable {

    var state : HandlerState = HandlerState.UNINITIALIZED
    private set

    private val timeoutRequests : MutableMap<Request, (Response) -> Unit> = mutableMapOf()

    private var timerIsInitialised = true
    private val requestTimer : Timer by lazy {
        synchronized(requestTimer) {
            timerIsInitialised = true
        }
        val timer = Timer(true)
        //to close the timer once it is no longer needed
        timer.scheduleAtFixedRate(1000, 1000) {
            if (synchronized(state) { state } > HandlerState.RUNNING &&
                synchronized(timeoutRequests) { timeoutRequests.isEmpty() }) {
                timer.cancel()
            }
        }
        return@lazy timer
    }

    private val trafficQueue : BlockingQueue<Traffic> = LinkedBlockingQueue()

    private val worker = Thread(::work, "OutgoingTrafficWorker")

    private fun work() {
        ObjectOutputStream(stream).use {
            while (synchronized(state) { state } == HandlerState.RUNNING || !trafficQueue.isEmpty()) {
                try {
                    val traffic = trafficQueue.take()
                    it.writeObject(traffic)
                } catch (_ : InterruptedException) {

                } catch (_ : Throwable) {
                    synchronized(state) {
                        state = HandlerState.ERROR
                        onError()
                    }
                }
            }
        }

        if (synchronized(state) { (state == HandlerState.CLOSING).also { if (it) state = HandlerState.CLOSED } }) {
            onClosed()
        }
    }

    fun start() {
        synchronized(state) {
            state = HandlerState.RUNNING
            worker.start()
        }
    }


    override fun close() {
        synchronized(state) {
            if (state >= HandlerState.CLOSING)
                return
            state = HandlerState.CLOSING
        }
        worker.interrupt()
    }

    fun sendTraffic(traffic : Traffic) {
        trafficQueue.put(traffic)
    }

    fun sendTimeoutRequest(
        request : Request,
        timeoutMillis : Long,
        onTimeout : () -> Unit,
        responseHandler : (Response) -> Unit
    ) {
        synchronized(timeoutRequests) {
            timeoutRequests += request to responseHandler
            requestTimer.schedule(timeoutMillis) {
                synchronized(timeoutRequests) {
                    timeoutRequests.remove(request)?.let {
                        onTimeout()
                    }
                }
            }
        }
        sendTraffic(request)
    }

    fun onResponseReceived(response : Response) {
        synchronized(timeoutRequests) {
            timeoutRequests.remove(response.to)
        }
    }
}