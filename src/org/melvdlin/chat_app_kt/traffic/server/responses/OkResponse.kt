package org.melvdlin.chat_app_kt.traffic.server.responses

import org.melvdlin.chat_app_kt.traffic.Request
import org.melvdlin.chat_app_kt.traffic.Response
import org.melvdlin.chat_app_kt.traffic.server.ServerTraffic

class OkResponse(override val to : Request) : ServerResponse