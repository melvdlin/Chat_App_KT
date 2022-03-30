package org.melvdlin.chat_app_kt.traffic.server.responses

import org.melvdlin.chat_app_kt.traffic.ErrorNotification
import org.melvdlin.chat_app_kt.traffic.Request

data class ServerErrorResponse(
    override val to : Request,
    override val fatal : Boolean,
    override val info : String
    ) : ServerResponse, ErrorNotification
{
    override fun toString() : String {
        return info
    }
}
