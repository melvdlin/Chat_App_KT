package org.melvdlin.chat_app_kt.core.netcode.impl

import org.melvdlin.chat_app_kt.core.client.Client
import org.melvdlin.chat_app_kt.core.traffic.Request
import org.melvdlin.chat_app_kt.core.traffic.Response
import org.melvdlin.chat_app_kt.core.traffic.Traffic
import org.melvdlin.chat_app_kt.core.traffic.server.responses.OkResponse
import java.io.ObjectOutputStream
import java.io.OutputStream
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.collections.HashMap
import kotlin.concurrent.schedule

internal class OutgoingTrafficHandler(
    private val stream : OutputStream,
    private val onClosed : () -> Unit,
    private val onError : () -> Unit
) : AutoCloseable {
    companion object Constants {
        private val illegalStates = arrayOf(
            HandlerState.CLOSING,
            HandlerState.CLOSED,
            HandlerState.ERROR
        )
    }

    val stateLock = Any()
    var state : HandlerState = HandlerState.UNINITIALIZED
    private set

    private var dcMode = DCMode.OTHER

    private var timeoutRequests : MutableMap<Request, (Response) -> Unit> = HashMap()

    private val requestTimer : Timer by lazy {
        Timer(true)
    }

    private val trafficQueue : BlockingQueue<Traffic> = LinkedBlockingQueue()

    val worker = Thread(::work)

    private fun work() {
        ObjectOutputStream(stream).use {
            while (
                !Thread.currentThread().isInterrupted &&
                (synchronized(stateLock) { state }
                == HandlerState.RUNNING
                || !trafficQueue.isEmpty())
            ) {
                try {
                    val traffic = trafficQueue.take()
                    it.writeObject(traffic)
                } catch (_ : InterruptedException) {
                    Thread.currentThread().interrupt()
                } catch (_ : Throwable) {
                    synchronized(stateLock) {
                        state = HandlerState.ERROR
                    }
                }
            }
        }
        if (Thread.currentThread().isInterrupted)
            return
        if (synchronized(stateLock) { state } == HandlerState.ERROR) {
            onError()
            return
        }
        if(synchronized(stateLock) {
            if (dcMode == DCMode.OTHER) {
                state = HandlerState.CLOSED
                return@synchronized true
            } else {
                return@synchronized false
            }
        }) {
            onClosed()
        }
    }

    fun start() {
        worker.start()
        synchronized(stateLock) {
            state = HandlerState.RUNNING
        }
    }

    fun kill() {
        worker.interrupt()
    }

    fun terminate(req : DisconnectRequest) {
        synchronized(stateLock) {
            state.ensureIsNoneOf(*illegalStates)
            state = HandlerState.CLOSING
            dcMode = DCMode.OTHER
        }
        trafficQueue.put(OkResponse(req))
    }

    override fun close() {
        synchronized(stateLock) {
            state.ensureIsNoneOf(*illegalStates)
            state = HandlerState.CLOSING
            dcMode = DCMode.SELF
        }
        val onReqFailed = {
            synchronized(stateLock) {
                state = HandlerState.ERROR
            }
            onError()
        }
        uncheckedSendTimeoutRequest(
            DisconnectRequest(),
            Client.Constants.timeoutMillis,
            onReqFailed
        ) {
            if (it !is OkResponse)
                onReqFailed()
        }
    }

    fun sendTraffic(traffic : Traffic) {
        synchronized(stateLock) {
            state.ensureIsNoneOf(*illegalStates)
        }
        trafficQueue.put(traffic)
    }

    fun sendTimeoutRequest(
        request : Request,
        timeoutMillis : Long,
        onTimeout : () -> Unit,
        responseHandler : (Response) -> Unit
    ) {
        synchronized(stateLock) {
            state.ensureIsNoneOf(*illegalStates)
        }
        uncheckedSendTimeoutRequest(
            request,
            timeoutMillis,
            onTimeout,
            responseHandler
        )
    }

    private fun uncheckedSendTimeoutRequest(
        request : Request,
        timeoutMillis : Long,
        onTimeout : () -> Unit,
        responseHandler : (Response) -> Unit
    ) {
        synchronized(timeoutRequests) {
            timeoutRequests += request to responseHandler
            requestTimer.schedule(timeoutMillis) {
                timeoutRequests.remove(request)?.let {
                    onTimeout()
                }
            }
        }
    }

    fun onResponseReceived(response : Response) {
        synchronized(timeoutRequests) {
            timeoutRequests.remove(response.to)?.invoke(response)
        }
    }
}