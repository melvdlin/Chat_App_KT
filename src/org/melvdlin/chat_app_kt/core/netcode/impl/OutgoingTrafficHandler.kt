package org.melvdlin.chat_app_kt.core.netcode.impl

import org.melvdlin.chat_app_kt.core.traffic.Request
import org.melvdlin.chat_app_kt.core.traffic.Response
import org.melvdlin.chat_app_kt.core.traffic.Traffic
import java.io.OutputStream
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.collections.HashMap

internal class OutgoingTrafficHandler(
    private val stream : OutputStream,
    private val onClosed : () -> Unit,
    private val onError : () -> Unit
) : AutoCloseable {

    val state : HandlerState
    get() {
        TODO("Not yet implemented")
    }

    private val timeoutRequests : Map<Request, () -> Unit> = HashMap()

    private val requestTimer : Timer by lazy {
        Timer(true)
    }

    private val trafficQueue : BlockingQueue<Traffic> = LinkedBlockingQueue()

    val worker = Thread(::work)

    private fun work() {

    }

    fun start() {
        TODO("Not yet implemented")
    }


    override fun close() {
        TODO("Not yet implemented")
    }

    fun sendTraffic(traffic : Traffic) {
        TODO("Not yet implemented")
    }

    fun sendTimeoutRequest(
        request : Request,
        timeoutMillis : Long,
        onTimeout : () -> Unit,
        responseHandler : (Response) -> Unit
    ) {
        TODO("Not yet Implemented")
    }

    fun onResponseReceived(response : Response) {
        TODO("Not yet Implemented")
    }
}