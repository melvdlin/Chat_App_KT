package org.melvdlin.chat_app_kt.core.traffic.server.responses

import org.melvdlin.chat_app_kt.core.traffic.ErrorNotification
import org.melvdlin.chat_app_kt.core.traffic.Request

data class ClientErrorResponse(
    override val to : Request,
    override val fatal : Boolean,
    override val info : String
    ) : ServerResponse, ErrorNotification
{
    override fun toString() : String {
        return info
    }
}