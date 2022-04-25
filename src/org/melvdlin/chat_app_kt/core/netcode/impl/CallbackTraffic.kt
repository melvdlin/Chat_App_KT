package org.melvdlin.chat_app_kt.core.netcode.impl

import org.melvdlin.chat_app_kt.core.traffic.Traffic

//consider making this a ConnectionHandler API feature
internal class CallbackTraffic(
    val payload : Traffic,
    val onSent : () -> Unit
) : Traffic