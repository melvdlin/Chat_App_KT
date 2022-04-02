package org.melvdlin.chat_app_kt.plugins.server.chat

import org.melvdlin.chat_app_kt.traffic.Traffic
import org.melvdlin.chat_app_kt.util.ConnectionHandler
import org.melvdlin.chat_app_kt.traffic.client.ClientTraffic
import org.melvdlin.chat_app_kt.traffic.client.requests.*
import org.melvdlin.chat_app_kt.traffic.server.MessageBroadcast
import org.melvdlin.chat_app_kt.traffic.server.responses.*

class ChatterConnection(private val messageLog : MessageLog, private val connectionHandler : ConnectionHandler) {

    private companion object ErrorResponseBuilder {

        enum class ClientErrorCause {
            UNKNOWN_TRAFFIC,
            CLIENT_ALREADY_LOGGED_IN,
            CLIENT_NOT_LOGGED_IN;

            val infoString : String get() {
                return when (this) {
                    UNKNOWN_TRAFFIC -> "Protocol violation: unknown traffic"
                    CLIENT_ALREADY_LOGGED_IN -> "Protocol violation: client is already logged in"
                    CLIENT_NOT_LOGGED_IN -> "Protocol violation: client is not logged in"
                    else -> ""
                }
            }
        }

        enum class ServerErrorCause {
            ;

            val infoString : String get() {
                return when (this) {
                    else -> ""
                }
            }
        }

        fun clientErrorResponse(to : ClientRequest, fatal : Boolean, cause : ClientErrorCause) : ClientErrorResponse {
            return ClientErrorResponse(to, fatal, cause.infoString)
        }

        fun serverErrorResponse(to : ClientRequest, fatal : Boolean, cause : ServerErrorCause) : ServerErrorResponse {
            return ServerErrorResponse(to, fatal, cause.infoString)
        }
    }

    private var chatterName : String? = null

    fun onTrafficReceived(traffic : Traffic) {
        when (traffic) {
            !is ClientTraffic -> {
                connectionHandler.close()
            }
            is LoginRequest -> {
                if (chatterName != null) {
                    connectionHandler.sendTraffic(
                        clientErrorResponse(
                            traffic,
                            true,
                            ClientErrorCause.CLIENT_ALREADY_LOGGED_IN
                        )
                    )
                    connectionHandler.close()
                } else {
                    chatterName = traffic.name
                    connectionHandler.sendTraffic(OkResponse(traffic))
                }
            }
            is FetchMessageLogRequest -> {
                if (chatterName == null) {
                    connectionHandler.sendTraffic(
                        clientErrorResponse(
                            traffic,
                            true,
                            ClientErrorCause.CLIENT_NOT_LOGGED_IN
                        )
                    )
                    connectionHandler.close()
                } else {
                    connectionHandler.sendTraffic(FetchMessageLogResponse(traffic, messageLog.toList(traffic.backlog)))
                }
            }
            is SendMessageRequest -> {
                if (chatterName == null) {
                    connectionHandler.sendTraffic(
                        clientErrorResponse(
                            traffic,
                            true,
                            ClientErrorCause.CLIENT_NOT_LOGGED_IN
                        )
                    )
                    connectionHandler.close()
                } else {
                    messageLog.addMessage(chatterName!!, traffic.msg)
                    connectionHandler.sendTraffic(OkResponse(traffic))
                }
            }
            is DisconnectRequest -> {
                connectionHandler.sendTraffic(OkResponse(traffic))
                connectionHandler.close()
            }
        }
    }

    fun sendMessage(msg : ChatMessage) = connectionHandler.sendTraffic(MessageBroadcast(msg))
}